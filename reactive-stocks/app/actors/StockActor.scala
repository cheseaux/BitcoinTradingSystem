package actors

import akka.actor.{ Props, ActorRef, Actor, Identify, ActorIdentity }
import utils.{ StockQuote, FakeStockQuote }
import java.util.Random
import scala.collection.immutable.{ HashSet, Queue }
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.libs.Akka
//import ch.epfl.bigdata.btc.crawler.coins.types.MarketPairTransaction
//import ch.epfl.bigdata.btc.crawler.coins.types.Market

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
    case WatchStock(_) =>
      // send the stock history to the user
      println("my name is \'" + self.path.name + "\'")
      println("my path is \'" + self.path + "\'")
      println("my parent is \'" + self.path.parent + "\'")
      println("my adress is \'" + self.path.address + "\'")

      sender ! StockHistory(symbol, stockHistory.asJava)

      println("initiating connection to DataSource: " + dataSourceSelection)
      dataSourceSelection.tell("connectionGUI", self);

      // add the watcher to the list
      watchers = watchers + sender

    // Response to connection to dataSource
    // store ref
    // start pull looper
    case dataSourceRef: ActorRef =>
      dataSourceActor = dataSourceRef
      println("remote actor ref obtained - YOUPI")

      launchScheduler;

    // called by scheduler
    // request value to dataSource
    case UpdateBitcoinValue =>
      dataSourceActor ! "BTCval"
      //            dataSourceActor ! MarketPairTransaction(Market.BTCe,null)
      println("new BTC value requested to " + dataSourceActor)

    // Response new value from dataSource
    // notify UserActor
    case valeur: Double =>
      println("new value received from space: " + valeur)
      val cinquante = stockQuote.newPrice(valeur)
      stockHistory = stockHistory.drop(1) :+ cinquante
      // notify watchers
      watchers.foreach(_ ! StockUpdate(symbol, cinquante))

    case "connection established - LOLcats on orbit" =>
      println("LoLcats acquired")

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
      watchers.foreach(_ ! StockUpdate(symbol, newPrice))
  }

  // A random data set which uses stockQuote.newPrice to get each data point
  var stockHistory: Queue[java.lang.Double] = {
    lazy val initialPrices: Stream[java.lang.Double] = (new Random().nextDouble * 800) #:: initialPrices.map(previous => stockQuote.newPrice(previous))
    initialPrices.take(50).to[Queue]
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

case class StockUpdate(symbol: String, price: Number)

case class StockHistory(symbol: String, history: java.util.List[java.lang.Double])

case class WatchStock(symbol: String)

case class UnwatchStock(symbol: Option[String])

