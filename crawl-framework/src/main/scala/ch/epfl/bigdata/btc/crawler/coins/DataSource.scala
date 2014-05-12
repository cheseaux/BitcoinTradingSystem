package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator
import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.CurrencyPair
import ch.epfl.bigdata.btc.crawler.coins.indicators.EMA
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
    case t: Tweet => updateAndSendTweet(t)
      //println("dataSource a recu les cadeaux: " + t.content)
      
        
    // Accepts registration for OHLC, Transaction, Twitters
    case mpro: MarketPairRegistrationOHLC => acceptRegistrationOHLC(mpro)
    case mprt: MarketPairRegistrationTransaction => acceptRegistrationTrans(mprt);
    case trf: TwitterRegistrationFull => acceptRegistrationTwitter(trf)
    case ir: EMARegistration => acceptIndicatorRegistration(ir)
  }
  
  def acceptIndicatorRegistration(a: Any) {
    if(!registrations.getIndicatorRegistrations().contains(a)) {
      registrations.addIndicator(a.asInstanceOf[IndicatorRegistration]);
      a match {
        case er: EMARegistration => 
          context.actorOf(Props(classOf[EMA], self, 
              MarketPairRegistrationOHLC(er.market, er.c, er.tickSize, er.tickCount), 100, 0.8), 
              er.market.toString + "_" + er.c.c1 + "-" +  er.c.c2 + "-" + er.tickSize + "-" +er.tickCount + "100-0.8")
      }
    }
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
    
    // distribution
    registrations.getTransRegByMarketPair(mp) match {
      case None => 
      case Some(l) => {
        l.map(a => a ! t)
println("sent transaction " + t)
      }
      
    }
    
    
    registrations.getOhlcRegByMarketPair(mp) match {
      case None => return
      case Some(l) => {
        l.map(a => {
          registrations.getOhlcRegByActor(a) match {
            case None => return
            case Some(k) => {
              k.map(mpro => a ! cache.getLatestOhlc(mpro))
		println("ohlc ")
            }
          }
          
         } 
        )
      }
      
    }
  }
  
  
  def updateAndSendTweet(t: Tweet) {
    cache.addTweet(t)
    registrations.getTwitterRegistrations.map(f => f ! t)
    println("sent tweet" + t)
  }
}
