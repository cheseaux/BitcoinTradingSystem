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
 
  var registrations = new Registrations()
  var cache = new Cache()
    
   
  def receive() = {
    // DataSource receives a transaction from its fetchers.
    case t: Transaction => updateCacheAndNotify(t)
    case t: Tweet => 
      //println("dataSource a recu les cadeaux: " + t.content)
      
        
    // Accepts registration for OHLC, Transaction, Twitters
    case mpro: MarketPairRegistrationOHLC => acceptRegistrationOHLC(mpro)
    case mprt: MarketPairRegistrationTransaction => acceptRegistrationTrans(mprt);
    case trf: TwitterRegistrationFull => acceptRegistrationTwitter(trf)
  }
  
  
  def acceptRegistrationOHLC(r: MarketPairRegistrationOHLC) {
    println("Registration for OHLC received from ", sender);
    registrations.addOhlcRegistration(r, sender)
    cache.addOhlcType(r)
    val registration = new MarketPair(r.market, r.c)
    pool ! registration
  }
    
  def acceptRegistrationTrans(r: MarketPairRegistrationTransaction) {
    println("Registration for Transaction received from ", sender);
    registrations.addTransRegistration(new MarketPair(r.market, r.c), sender);
    val registration = new MarketPair(r.market, r.c)
    pool ! registration
    
    val trans = cache.getAllTransByMarketPair(new MarketPair(r.market, r.c))
    trans.map(t => sender ! t)
  }
     
  def acceptRegistrationTwitter(r: TwitterRegistrationFull) {
    println("Registration for Twitter received from ", sender);
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
    
    
    registrations.getOhlcRegByMarketPair(mp) match {
      case None => return
      case Some(l) => {
        l.map(a => {
          registrations.getOhlcRegByActor(a) match {
            case None => return
            case Some(k) => {
              k.map(mpro => a ! cache.getLatestOhlc(mpro))
            }
          }
          
         } 
        )
      }
      
    }
  }
}