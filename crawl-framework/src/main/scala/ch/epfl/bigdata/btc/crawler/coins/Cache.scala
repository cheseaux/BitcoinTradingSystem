package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator
import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.CurrencyPair
import scala.collection.mutable.MutableList
import scala.collection.mutable.HashMap
import org.joda.time.Duration
import org.joda.time.DateTime

/**
 * This is the internal cache for OHLC, Transactions and Tweets
 */
class Cache {
  private val ohlcByMp = new HashMap[MarketPair, MutableList[MarketPairRegistrationOHLC]]()
  private val twitter = new MutableList[Tweet]()
  private val ohlc = new HashMap[MarketPairRegistrationOHLC, MutableList[OHLC]]()
  private val trans = new HashMap[MarketPairRegistrationTransaction, MutableList[Transaction]]()

  /**
   * Add a new Tweet to the cache
   */
  def addTweet(t: Tweet) {
    twitter.+=:(t)
  }

  /**
   * Add a new Transaction to the cache
   */
  def addTransaction(mp: MarketPairRegistrationTransaction, t: Transaction) {
    trans.get(mp) match {
      case None => trans += (mp -> ((new MutableList[Transaction]) += t))
      case Some(m) => m += t
    }
  }

  /**
   * Tested and works
   */
  // oldest - - - - - newest
  // head   - - - - - last
  def updateOhlcForMpro(mp: MarketPairRegistrationOHLC, o: OHLC) {
    ohlc.get(mp) match {
      case Some(l) => {
        if (l.length == 0) {
          l += o
          return
        }
        println("updateOhlcForMpro:updateOhlcForMpro: " + mp + " " + l.length + " " + l.last + " " + l.head)
        var head = l.head
        var last = l.last
        var currentTime = o.date
        var currentIndex = (o.date.minus(l.last.date.getMillis()).getMillis().toInt / 1000) / (o.duration.getMillis() / 1000)

        // currentIndex == 0 => update last
        // currentIndex  > 0 => this one is newer -> append and fill
        // currentindex  < 0 => this one is older -> prepend and fill

        println("updateOhlcForMpro:currentIndex", currentIndex)
        if (currentIndex == 0) {
          println("updateOhlcForMpro:l.update", o)
          l.update(l.length-1, o)
        } else if (currentIndex > 0) {
          var toAddDate = last.date
          for (i <- 1 to currentIndex.toInt) {
            toAddDate = toAddDate.plus(o.duration)
            l += new OHLC(last.close, last.close, last.close, last.close, 0,
              new DateTime(toAddDate), new Duration(o.duration))
          }
          l.update(l.length - 1, o)
        } else if (currentIndex < 0) {

          if (-currentIndex >= l.length) {
            var copy = l;
            var toAddDate = head.date
            currentIndex = -currentIndex;
            for (i <- 1 to currentIndex.toInt) {
              toAddDate = toAddDate.minus(o.duration)
              copy = new OHLC(head.open, head.open, head.open, head.open, 0,
                new DateTime(toAddDate), new Duration(o.duration)) +: copy
            }
            copy.update(0, o)
            ohlc.put(mp, copy)
          } else {
            l.update(l.length + (currentIndex.toInt) - 1, o)
          }
        }
      }
      case None => return
    }
  }

  /**
   * Should work
   */
  def getOhlcByTimestampAndMpro(mp: MarketPairRegistrationOHLC, t: DateTime): OHLC = {
    var d = new Duration(mp.tickSize * 1000);
    var ts = new DateTime(t.getMillis() - (t.getMillis() % (mp.tickSize * 1000)))

    println("d", d) // OK
    println("ts", ts) // OK
   

    ohlc.get(mp) match {
      case Some(l) => {
         println("l",  l)
        if (l.length == 0) {
          return new OHLC(Double.MinValue, Double.MinValue, Double.MaxValue, 0.0, 0.0, ts, d)
        }
        var latest = l.last;
        //var getIndex = (latest.date.minus(t.getMillis()).getMillis().toInt / 1000) / mp.tickSize

        var getIndex = ((ts.minus(l.last.date.getMillis()).getMillis()).toInt / 1000) / (mp.tickSize) // OK

        println("getIndex", getIndex)
        println("l.length", l.length)
        println("l.get", l.length + getIndex - 1)

        if (-getIndex > l.length - 1) {
          return new OHLC(Double.MinValue, Double.MinValue, Double.MaxValue, 0.0, 0.0, ts, d)
        } else {
          l.get(l.length + getIndex - 1) match {
            case Some(n) => { println("SOME", n); n }
            case None => {
              println("NONE");
              return new OHLC(Double.MinValue, Double.MinValue, Double.MaxValue, 0.0, 0.0, ts, d)
            }
          }
        }

      }
      case None => return new OHLC(Double.MinValue, Double.MinValue, Double.MaxValue, 0.0, 0.0, ts, d)
    }
  }

  def updateOHLC(mp: MarketPair, t: Transaction) {
    ohlcByMp.get(mp) match {
      case None => return
      case Some(l) => l.map(mpro => {

        println("updateOHLC:UPDATE OHLC", t)
        var ohlc = getOhlcByTimestampAndMpro(mpro, t.timestamp)
        println("updateOHLC:getOhlcByTimestampAndMpro", ohlc)
        ohlc = updateGivenOHLC(ohlc, t)
        println("updateOHLC:updateGivenOHLC", ohlc)
        updateOhlcForMpro(mpro, ohlc)
      })
    }
  }

  def updateGivenOHLC(o: OHLC, t: Transaction): OHLC = {
    var open = if (o.open == Double.MinValue) t.unitPrice else o.open
    var high = if (o.high > t.unitPrice) o.high else t.unitPrice
    var low = if (o.low < t.unitPrice) o.low else t.unitPrice
    var close = t.unitPrice
    var volume = o.volume + t.amount
    var date = o.date
    var duration = o.duration
    return new OHLC(open, high, low, close, volume, date, duration)
  }

  /**
   * Initializes a new MarketPair for OHLC
   */
  def addOhlcType(mpro: MarketPairRegistrationOHLC) {
    val mp = new MarketPair(mpro.market, mpro.c)
    ohlc.get(mpro) match { // Check if mpro is already registered
      case None => ohlc += (mpro -> new MutableList[OHLC])
      case _ =>
    }
    ohlcByMp.get(mp) match { // check if mp to mpro is registered
      case None => ohlcByMp += (mp -> ((new MutableList[MarketPairRegistrationOHLC]) += mpro))
      case Some(l) => {
        if (!l.contains(mpro)) {
          l += mpro
        }
      }
    }
  }

  def getLatestOhlc(mpro: MarketPairRegistrationOHLC) = {
    ohlc.get(mpro) match {
      case None => new OHLC(0.0, Int.MinValue, Int.MaxValue, 0.0, 0.0,
        new DateTime(), new Duration(mpro.tickSize * 1000))
      case Some(l) => l.tail
    }
  }

  def getAllOhlc(mpro: MarketPairRegistrationOHLC) = {
    ohlc.get(mpro) match {
      case None => new MutableList[OHLC]()
      case Some(l) => l
    }
  }

  def getAllTransByMarketPair(mp: MarketPair) = {
    trans.get(new MarketPairRegistrationTransaction(mp.market, mp.c)) match {
      case None => new MutableList[Transaction]
      case Some(l) => l;
    }
  }

}