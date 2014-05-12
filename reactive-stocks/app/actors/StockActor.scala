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

/**
 * There is one StockActor per stock symbol.  The StockActor maintains a list of users watching the stock and the stock
 * values.  Each StockActor updates a rolling dataset of randomly generated stock values.
 */

class StockActor(symbol: String) extends Actor {

  lazy val stockQuote: StockQuote = new FakeStockQuote

  // remote dataSource address
  val dataSourceSelection = context.actorSelection("akka.tcp://DataSourceSystem@127.0.0.1:2553/user/DataSource")
  // variable used to store ref
  var dataSourceActor: ActorRef = null

  // initialisé à l'instanciation - the actor that created it is inside
  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]

  // TODO: template scheduler - unused now
  // Fetch the latest stock value every 75ms
  //  val stockTick = context.system.scheduler.schedule(Duration.Zero, 75.millis, self, FetchLatest)

  // TODO: make stockTick variable global so that we can kill it when app closes on UnwatchStock msg received
  // method used to launche scheduler
  def launchScheduler = {
    val stockTick = context.system.scheduler.schedule(Duration.Zero, 2000.millis, self, UpdateBitcoinValue)

  }

  def receive = {

    // on creation
    // send History back to UserActor 
    // initiate connection with dataSource
    // send the stock history to the user)

    case WatchStock(_) =>

      // TODO: keep for beauty, remove if nice
      sender ! StockHistory(symbol, stockHistory.asJava)

      println("initiating connection to DataSource: " + dataSourceSelection)

      // register with DataSource actor
      dataSourceSelection ! MarketPairRegistrationTransaction(Market.BTCe, CurrencyPair(Currency.USD, Currency.BTC))
      dataSourceSelection ! TwitterRegistrationFull()
      dataSourceSelection ! EMARegistration(Market.BTCe, CurrencyPair(Currency.USD, Currency.BTC), 26, 30)
      dataSourceSelection ! SMARegistration(Market.BTCe, CurrencyPair(Currency.USD, Currency.BTC), 26, 30)
      // add the watcher to the list
      watchers = watchers + sender

    case points: Points =>
      points.ind match {
        case Indicator.EMA =>
          // send as EMA
          println("received EMA, first val: " + points.values.last._1)
        case Indicator.SMA =>
          // send as SMA
          println("received EMA, first val: " + points.values.last._1)

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
      val time = transaction.timestamp.getMillis() / 1000;
      watchers.foreach(_ ! StockUpdate(symbol, price, time))

    case tweet: Tweet =>
      println("got a new tweet: " + tweet.content)
      watchers.foreach(_ ! tweet)

    // called when killing app
    case UnwatchStock(_) =>
      watchers = watchers - sender
      if (watchers.size == 0) {
        //stockTick.cancel()
        context.stop(self)
      }

    // TODO: initial template code
    case FetchLatest =>
      // add a new stock price to the history and drop the oldest
      // TODO: modify price fetching here
      val newPrice = stockQuote.newPrice(stockHistory.last.doubleValue())
      stockHistory = stockHistory.drop(1) :+ newPrice
      // notify watchers
      watchers.foreach(_ ! StockUpdate(symbol, newPrice, 0))
  }

  // TODO: remove
  // A random data set which uses stockQuote.newPrice to get each data point
  //  var stockHistory: Queue[java.lang.Double] = {
  //    lazy val initialPrices: Stream[java.lang.Double] = (new Random().nextDouble * 800) #:: initialPrices.map(previous => stockQuote.newPrice(previous))
  //    initialPrices.take(50).to[Queue]
  //  }
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

case object UpdateBitcoinValue

case object FetchLatest

case class StockUpdate(symbol: String, price: Number, time: Long)

case class StockHistory(symbol: String, history: java.util.List[java.lang.Double])

case class WatchStock(symbol: String)

case class UnwatchStock(symbol: Option[String])

// TODO: 
case class EMAupdate(symbol: String)

//// TODO: remove when Marzell's is here
//case class OHLC

