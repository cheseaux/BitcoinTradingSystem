package actors

import java.util.Random
import scala.collection.JavaConverters._
import scala.collection.immutable.HashSet
import scala.collection.immutable.Queue
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import play.libs.Akka
import utils.FakeStockQuote
import utils.StockQuote
import ch.epfl.bigdata.btc.types._
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.Transfer._
import scala.collection.JavaConverters._
import com.fasterxml.jackson.databind.JsonNode
import play.libs.Json
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor
import com.fasterxml.jackson.databind.node.ArrayNode
import play.api.libs.json.JsArray
import play.api.libs.json.JsString
import org.json.JSONArray
import org.json.JSONObject
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import java.util.Collection

/**
 * There is one StockActor per stock symbol.  The StockActor maintains a list of users watching the stock and the stock
 * values.  Each StockActor updates a rolling dataset of randomly generated stock values.
 */

class StockActor(symbol: String) extends Actor {

  
  // parameters for EMA/SMA
//  val TICK_SIZE = 26
//  val TICK_COUNT = 10
  val TICK_COUNT = 26
  val TICK_SIZE = 10
  val PERCENTAGE = 0.6

  // remote dataSource address
  val dataSourceSelection = context.actorSelection("akka.tcp://DataSourceSystem@127.0.0.1:2553/user/DataSource")
  // variable used to store ref
  var dataSourceActor: ActorRef = null

  // TODO: remplacer par le websocket
  // initialisé à l'instanciation - the actor that created it is inside
  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]


  def receive = {

    // on creation
    // send History back to UserActor 
    // initiate connection with dataSource
    // send the stock history to the user)
    case WatchStock(_) =>

      sender ! StockHistory(symbol, stockHistory.asJava)

      println("initiating connection to DataSource: " + dataSourceSelection)

      // register with DataSource actor
      dataSourceSelection ! MarketPairRegistrationTransaction(Market.BTCe, CurrencyPair(Currency.USD, Currency.BTC))
      dataSourceSelection ! TwitterRegistrationFull()
      
      
      dataSourceSelection ! EMARegistration(Market.BTCe, CurrencyPair(Currency.USD, Currency.BTC), TICK_SIZE, TICK_COUNT, PERCENTAGE)
      dataSourceSelection ! SMARegistration(Market.BTCe, CurrencyPair(Currency.USD, Currency.BTC), TICK_SIZE, TICK_COUNT)
      // add the watcher to the list
      watchers = watchers + sender

    case points: Points =>
      points.ind match {
        case Indicator.EMA =>

          
          println("StockActor: received EMA")

          // parse and create JSON
          var array = points.values.map( e => {
            var inner = JsonNodeFactory.instance.arrayNode();
            inner.insert(0, e._2)
            inner.insert(1, e._1)
            inner
          })
          var data : ArrayNode = JsonNodeFactory.instance.arrayNode();
          
          var javaArray: java.util.List[ArrayNode] = array.asJava
          data.addAll(javaArray.asInstanceOf[java.util.Collection[JsonNode]] )
          
          
          val jsonEMA = Json.newObject();
          jsonEMA.put("values", data)
          jsonEMA.put("type", "EMA");
          
          println("EMA Json sent to GUI: " + jsonEMA)
          
          // send the stuff
          watchers.foreach(_ ! EMAupdate(jsonEMA))

        case Indicator.SMA =>
          // send as SMA
          println("StockActor: received SMA")

          // parse and create JSON
          var array = points.values.map( e => {
            var inner = JsonNodeFactory.instance.arrayNode();
            inner.insert(0, e._2)
            inner.insert(1, e._1)
            inner
          })
          var data : ArrayNode = JsonNodeFactory.instance.arrayNode();
          
          var javaArray: java.util.List[ArrayNode] = array.asJava
          data.addAll(javaArray.asInstanceOf[java.util.Collection[JsonNode]] )
          
          
          val jsonSMA = Json.newObject();
          jsonSMA.put("values", data)
          jsonSMA.put("type", "SMA");
          
          println("SMA Json sent to GUI: " + jsonSMA)
          
          // send the stuff to userActor
          watchers.foreach(_ ! SMAupdate(jsonSMA))

      }

    case ohlc: OHLC =>
      //
      //      println("new value received from space: " + 700)
      //      val cinquante = stockQuote.newPrice(700)
      //      stockHistory = stockHistory.drop(1) :+ cinquante
      // notify watchers
      watchers.foreach(_ ! ohlc)

    case transaction: Transaction =>
      val price: Double = transaction.unitPrice;
      val time = transaction.timestamp.getMillis()
      println("received transaction update, time: " + transaction.timestamp)
      watchers.foreach(_ ! StockUpdate(symbol, price, time))

    case tweet: Tweet =>
      watchers.foreach(_ ! tweet)

    // called when killing app
    case UnwatchStock(_) =>
      watchers = watchers - sender
      if (watchers.size == 0) {
        //stockTick.cancel()
        context.stop(self)
      }
    case _ =>
      println("StockActor: received unknown type")

  }

  // shit legacy stuff required to run, DO NOT REMOVE
  lazy val stockQuote: StockQuote = new FakeStockQuote
  
  var stockHistory: Queue[java.lang.Double] = {
    lazy val initialPrices: Stream[java.lang.Double] = (0) #:: initialPrices.map(previous => stockQuote.newPrice(previous))
    initialPrices.take(5).to[Queue]
  }

}

class StocksActor extends Actor {
  def receive = {
    case watchStock @ WatchStock(symbol) =>
      // get or create the StockActor for the symbol and forward this message
      context.child(symbol).getOrElse {
        // create StockActor
        context.actorOf(Props(new StockActor(symbol)), symbol)
      } forward watchStock
    case unwatchStock @ UnwatchStock(Some(symbol)) =>
      // if there is a StockActor for the symbol forward this message
      context.child(symbol).foreach(_.forward(unwatchStock))
    case unwatchStock @ UnwatchStock(None) =>
      // if no symbol is specified, forward to everyone
      context.children.foreach(_.forward(unwatchStock))
  }
}

object StocksActor {
  lazy val stocksActor: ActorRef = Akka.system.actorOf(Props(classOf[StocksActor]))
}


case class StockUpdate(symbol: String, price: Number, time: Long)

case class StockHistory(symbol: String, history: java.util.List[java.lang.Double])

case class WatchStock(symbol: String)

case class UnwatchStock(symbol: Option[String])

case class EMAupdate(json: JsonNode)
case class SMAupdate(json: JsonNode)


