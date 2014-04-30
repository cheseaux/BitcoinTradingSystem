package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator
import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.crawler.coins.types.Transaction
import ch.epfl.bigdata.btc.crawler.coins.types.OHLC
import ch.epfl.bigdata.btc.crawler.coins.types.MarketPair
import ch.epfl.bigdata.btc.crawler.coins.types.MarketPairRegistration
import ch.epfl.bigdata.btc.crawler.coins.types.MarketPairTransaction
import scala.collection.mutable.MutableList
import scala.collection.mutable.HashMap
import akka.event.Logging
import akka.actor.{Actor, ActorRef, Props}
import ch.epfl.bigdata.btc.crawler.coins.types.MarketPairRegistration
import ch.epfl.bigdata.btc.crawler.coins.types.Transaction
import ch.epfl.bigdata.btc.crawler.coins.types.CurrencyPair


class DataSource() extends Actor {
  import context._
  val pool = context.actorOf(Props[MarketFetchPool], "MarketFetchPool")
  val log = Logging(context.system, this)
  var last = System.currentTimeMillis();
  var lastReceive = System.currentTimeMillis()
  var lists = new HashMap[MarketPairRegistration, MutableList[OHLC]]()
  var subscriber = new HashMap[MarketPairRegistration, MutableList[Tuple2[MarketPair, ActorRef]]]()
  var cache = new HashMap[MarketPairRegistration, MutableList[Transaction]]()
  log.debug("DataSource: initialized")
   
  def receive() = {
    case t: Transaction => updateCache(t)
    case r: MarketPair => acceptRegistration(r) // register in fetcher pool, setup mapper,
    case mpr: MarketPairRegistration => pool ! mpr // send this to register
    case mpt: MarketPairTransaction => println("ilia");
    
    // TODO: cases used to test connection to GUI
    case "connectionGUI" => 
      println("received connection msg from GUI")
      sender ! self
    case "BTCval" => 
      println("new BTC value request received")
      // valeur arbitraire, envoyer autre message depuis GUI
      sender ! 678.88 
      
  }
  
  def acceptRegistration(mp: MarketPair){
    log.debug("acceptRegistration 1")
    val registration  = new MarketPairRegistration(mp.market, mp.c)
    println("Register " + mp.market + " " + mp.c)
    subscriber.get(registration) match {
      case None => subscriber += (registration -> (new MutableList[Tuple2[MarketPair, ActorRef]]() += new Tuple2(mp, sender)))
      case Some(m) => m += new Tuple2(mp, sender)
    }
    lists += (registration -> new MutableList[OHLC]())
    log.error("acceptRegistration 2")
	pool ! registration
  }
  
  def updateCache(t: Transaction) {
    if(last + 10000 < System.currentTimeMillis()) {
      last = System.currentTimeMillis();
      log.debug("DataSource.sendTo")
    }
    
    val newOne = MarketPairRegistration(t.market, new CurrencyPair(t.from, t.to))
    cache.get(newOne) match {
      case None => cache += (newOne -> (new MutableList[Transaction]() += t))
      case Some(m) => m += t
    } 
  }
  
  def distribute(mpr: MarketPairRegistration) {
    subscriber.get(mpr) match {
      case Some(m) => m.map(f => sendTo(f._1, f._2))
      case None =>
    }
  }
  
  def sendTo(mp: MarketPair, dest: ActorRef) {
    
  }
  
  
}