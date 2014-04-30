package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator
import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.CurrencyPair
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
  
  
  /**
   * This is the internal cache
   */
  class Cache {
    private val twitter = new MutableList[Tweet]()
    private val ohlcByMp = new HashMap[MarketPair, MutableList[MarketPairRegistrationOHLC]]()
    private val ohlc = new HashMap[MarketPairRegistrationOHLC, MutableList[OHLC]]()
    private val trans = new HashMap[MarketPairRegistrationTransaction, MutableList[Transaction]]()
    
    
    def addTwitter(t : Tweet) {
      twitter += t
    }
    
    def addTransaction(mp : MarketPairRegistrationTransaction, t : Transaction) {
      trans.get(mp) match {
        case None => trans += (mp -> ((new MutableList[Transaction])+= t))
        case Some(m) => m += t
      }
    }
    
    def addOhlcType(mpro: MarketPairRegistrationOHLC) {
      val mp = new MarketPair(mpro.market, mpro.c)
      ohlc.get(mpro) match {
        case None => ohlc += (mpro -> new MutableList[OHLC])
      }
      ohlcByMp.get(mp) match {
        case None => ohlcByMp += (mp -> ((new MutableList[MarketPairRegistrationOHLC]) += mpro))
        case Some(l) => { 
          if(!l.contains(mpro)) {
            l += mpro
          }
        }
      }
    }
    
    def updateOHLC(mp: MarketPair, t: Transaction) {
      
    }
    
    def getLatestOhlc(mpro: MarketPairRegistrationOHLC) {
      return 
    }
    
    private def updateOhlcOfMpro(mp : MarketPairRegistrationOHLC, t : OHLC) {
      ohlc.get(mp) match {
        case None => ohlc += (mp -> ((new MutableList[OHLC])+= t))
        case Some(m) => m += t
      }
    }    
  }
  
  
  
  /**
   * This is the internal observer store
   */
  class Registrations {
	  val twitter = new MutableList[ActorRef]()
	  val ohlc = new HashMap[MarketPair, MutableList[ActorRef]]()
	  val trans = new HashMap[MarketPair, MutableList[ActorRef]]()
	  
	  val actorsOhlc = new HashMap[ActorRef, MutableList[MarketPairRegistrationOHLC]]()
	  val actorsTrans = new HashMap[ActorRef, MutableList[MarketPair]]()
	  
	  def getTwitterRegistrations() = twitter
	  def getOhlcRegByMarketPair(mp : MarketPair) = ohlc.get(mp)
	  def getTransRegByMarketPair(mp : MarketPair) = trans.get(mp)
	  
	  def getOhlcRegByActor(a : ActorRef) = actorsOhlc.get(a)
	  def getTransRegByActor(a : ActorRef) = actorsTrans.get(a)
	  
	  def addTwitterRegistration(a: ActorRef) {
	    twitter += a
	  }
	  def addOhlcRegistration(mpro : MarketPairRegistrationOHLC, a: ActorRef) {
	    val mp = new MarketPair(mpro.market, mpro.c)
	    ohlc.get(mp) match {
	     case None => ohlc += (mp -> ((new MutableList[ActorRef]()) += a))
	     case Some(m) => m += a
	    }
	    actorsOhlc.get(a) match {
	     case None => actorsOhlc += (a -> ((new MutableList[MarketPairRegistrationOHLC]()) += mpro))
	     case Some(m) => m += mpro
	    }
	  }
	  def addTransRegistration(mp : MarketPair, a: ActorRef) {
	    trans.get(mp) match {
	     case None => trans += (mp -> ((new MutableList[ActorRef]()) += a))
	     case Some(m) => m += a
	    }
	    actorsTrans.get(a) match {
	     case None => actorsTrans += (a -> ((new MutableList[MarketPair]()) += mp))
	     case Some(m) => m += mp
	    }
	  }
  }
  
  var registrations = new Registrations()
  var cache = new Cache()
  
  
  
  
   
  def receive() = {
    

    // DataSource receives a transaction from its fetchers.
    case t: Transaction => updateCacheAndNotify(t)
        
    // Accepts registration for OHLC, Transaction, Twitters
    case mpro: MarketPairRegistrationOHLC => acceptRegistrationOHLC(mpro)
    case mprt: MarketPairRegistrationTransaction => acceptRegistrationTrans(mprt);
    case trf: TwitterRegistrationFull => acceptRegistrationTwitter(trf)
  }
  
  
  def acceptRegistrationOHLC(r: MarketPairRegistrationOHLC) {
    registrations.addOhlcRegistration(r, sender)
    cache.addOhlcType(r)
    val registration = new MarketPair(r.market, r.c)
    pool ! registration
  }
    
  def acceptRegistrationTrans(r: MarketPairRegistrationTransaction) {
    registrations.addTransRegistration(new MarketPair(r.market, r.c), sender);
    val registration = new MarketPair(r.market, r.c)
    pool ! registration
  }
     
  def acceptRegistrationTwitter(r: TwitterRegistrationFull) {
    registrations.addTwitterRegistration(sender)
  }
  
  
  /**
   * update the cache and the distribute
   */
  def updateCacheAndNotify(t: Transaction) {
    
   
    
    val mprt = MarketPairRegistrationTransaction(t.market, new CurrencyPair(t.from, t.to))
    val mp = MarketPair(t.market, new CurrencyPair(t.from, t.to))
    
    // update cache
    cache.addTransaction(mprt, t)
    cache.updateOHLC(mp, t)
    
    // distriution
    registrations.getTransRegByMarketPair(mp) match {
      case Some(l) => {
        l.map(a => a ! t)
      }
      case None => 
    }
    /*
    registrations.getOhlcRegByMarketPair(mp) match {
      case Some(l) => {
        l.map(a => {
          registrations.getOhlcRegByActor(a) match {
            case Some(k) => {
              k.map(mpro => println(mpro))
            }
            case None => 
          }
          
         } 
        )
      }
      case None => 
    }
    */
    
    
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