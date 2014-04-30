package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator
import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Registration._
import scala.collection.mutable.MutableList
import scala.collection.mutable.HashMap
import akka.event.Logging
import akka.actor.{Actor, ActorRef, Props}


class DataSource() extends Actor {
  import context._
  val pool = context.actorOf(Props[MarketFetchPool], "MarketFetchPool")
  val log = Logging(context.system, this)
  var last = System.currentTimeMillis();
  var lastReceive = System.currentTimeMillis()
  //var lists = new HashMap[MarketPairRegistration, MutableList[OHLC]]()
  //var subscriber = new HashMap[MarketPairRegistration, MutableList[Tuple2[MarketPair, ActorRef]]]()
  //var cache = new HashMap[MarketPairRegistration, MutableList[Transaction]]()
  log.debug("DataSource: initialized")
  
  
  class Registrations {
	  var twitter = new MutableList[ActorRef]()
	  var ohlc = new HashMap[MarketPair, MutableList[ActorRef]]()
	  var trans = new HashMap[MarketPair, MutableList[ActorRef]]()
	  
	  def getTwitterRegistrations () = twitter
	  def getOhlcRegistration(mp : MarketPair) = ohlc.get(mp)
	  def getTransRegistration(mp : MarketPair) = trans.get(mp)
	  
	  def addTwitterRegistration(a: ActorRef) {
	    twitter += a
	  }
	  def addOhlcRegistration(mp : MarketPair, a: ActorRef) {
	    ohlc.get(mp) match {
	     case None => ohlc += (mp -> ((new MutableList[ActorRef]()) += a))
	     case Some(m) => m += a
	    }
	  }
	  def addTransRegistration(mp : MarketPair, a: ActorRef) {
	    trans.get(mp) match {
	     case None => trans += (mp -> ((new MutableList[ActorRef]()) += a))
	     case Some(m) => m += a
	    }
	  }
  }
  
  var registrations = new Registrations()
  
  
  
  
   
  def receive() = {
    
    // DataSource receives a transaction from its fetchers.
    //case t: Transaction => updateCache(t)
        
    // Accepts registration for OHLC, Transaction, Twitters
    case mpro: MarketPairRegistrationOHLC => acceptRegistrationOHLC(mpro)
    case mprt: MarketPairRegistrationTransaction => acceptRegistrationTrans(mprt);
    case trf: TwitterRegistrationFull => acceptRegistrationTwitter(trf)
  }
  
  
  def acceptRegistrationOHLC(r: MarketPairRegistrationOHLC) {
    
  }
  
  
  def acceptRegistrationTrans(r: MarketPairRegistrationTransaction) {
    
  }
   
   
  def acceptRegistrationTwitter(r: TwitterRegistrationFull) {
    
  }
  
  
  
  
  
  
  
  /*
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
  */
  
  
}