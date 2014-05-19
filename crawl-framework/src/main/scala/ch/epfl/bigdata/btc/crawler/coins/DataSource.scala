package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins._;
import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator
import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.CurrencyPair
import ch.epfl.bigdata.btc.crawler.coins.indicators.EMA
import ch.epfl.bigdata.btc.crawler.coins.indicators.SMA 
import scala.collection.mutable.MutableList
import scala.collection.mutable.HashMap
import akka.event.Logging
import akka.actor.{ Actor, ActorRef, Props }

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

    // Accepts registration for OHLC, Transaction, Tweets
    case mpro: MarketPairRegistrationOHLC => acceptRegistrationOHLC(mpro)
    case mprt: MarketPairRegistrationTransaction => acceptRegistrationTrans(mprt);
    case trf: TwitterRegistrationFull => acceptRegistrationTwitter(trf)
    case ir: IndicatorRegistration => acceptIndicatorRegistration(ir)
  }

  def acceptIndicatorRegistration(a: IndicatorRegistration) {
    var observer = sender;
    var ir = a;

    registrations.getIndicatorRegistrations().get(ir) match {
      case Some(indicator) => {
        indicator ! observer
        println("already register indicator, oh non, c'est balot !!! :( ", ir)
      }
      case None => // create new, enreg
        {
          ir match {
            case er: EMARegistration => {
              var indicator = context.actorOf(Props(classOf[EMA], self,
                MarketPairRegistrationOHLC(er.market, er.c, er.tickSize, er.tickCount), er.tickCount, er.percent),
                "EMA" + er.market.toString + "_" + er.c.c1 + "-" + er.c.c2 + "-" + er.tickSize + "-" + er.tickCount + "_" + er.tickCount + "-" + er.percent)
                registrations.addIndicator(ir, indicator)
                indicator ! observer
                println("EMARegistration already register indicator, oh non, c'est balot !!! :( ", er)
            }
            case er: SMARegistration => {
              var indicator = context.actorOf(Props(classOf[SMA], self,
                MarketPairRegistrationOHLC(er.market, er.c, er.tickSize, er.tickCount), er.tickCount),
                "SMA" + er.market.toString + "_" + er.c.c1 + "-" + er.c.c2 + "-" + er.tickSize + "-" + er.tickCount)
                registrations.addIndicator(ir, indicator)
                indicator ! observer
                println("SMARegistration already register indicator, oh non, c'est balot !!! :( ", er)
            }
            case _ => println("Could not register")
          }
        }
    }
    println("sent");

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

    //println(t)
    val mprt = MarketPairRegistrationTransaction(t.market, new CurrencyPair(t.from, t.to))
    val mp = MarketPair(t.market, new CurrencyPair(t.from, t.to))

    // update cache
    cache.addTransaction(mprt, t)
    cache.updateOHLC(mp, t)

    // distribution
    registrations.getTransRegByMarketPair(mp) match {
      case None =>
      case Some(l) => { //println("getTransRegByMarketPair", mp)
        l.map(a => a ! t)
      }

    }

    registrations.getOhlcRegByMarketPair(mp) match {
      case None => return
      case Some(l) => {
        l.map(a => {
          registrations.getOhlcRegByActor(a) match {
            case None => return
            case Some(mproList) => {
              mproList.map(e => {
                if (e.market == mp.market && e.c == mp.c) {
                  //println("send to ", a)
                  a ! cache.getLatestOhlc(e)
                }
              })
            }
          }

        })
      }

    }
  }

  def updateAndSendTweet(t: Tweet) {
    cache.addTweet(t)
    registrations.getTwitterRegistrations.map(f => f ! t)
    println("sent tweet" + t)
  }
}
