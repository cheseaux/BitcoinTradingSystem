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
    def addTwitter(t : Tweet) {
      twitter.+=:(t)
    }
    
	 /**
     * Add a new Transaction to the cache
     */
    def addTransaction(mp : MarketPairRegistrationTransaction, t : Transaction) {
      trans.get(mp) match {
        case None => trans += (mp -> ((new MutableList[Transaction])+= t))
        case Some(m) => m.+=:(t)
      }
    }
    
     /**
     * Initializes a new MarketPair for OHLC
     */
    def addOhlcType(mpro: MarketPairRegistrationOHLC) {
      val mp = new MarketPair(mpro.market, mpro.c)
      ohlc.get(mpro) match { // Check if mpro is already registered
        case None => ohlc += (mpro -> new MutableList[OHLC])
        case _ => return
      }
      ohlcByMp.get(mp) match { // check if mp to mpro is registered
        case None => ohlcByMp += (mp -> ((new MutableList[MarketPairRegistrationOHLC]) += mpro))
        case Some(l) => { 
          if(!l.contains(mpro)) {
            l += mpro
          }
        }
      }
    }
    
    /**
     * Update the latest OHLC for a given MarketPair with the data of the transaction
     */
    def updateOHLC(mp: MarketPair, t: Transaction) {
      ohlcByMp.get(mp) match {
        case None => return
        case Some(l) => l.map(mpro => {
          ohlc.get(mpro) match {
	        case None => return
	        case Some(l) => {
	          var time = (t.timestamp.getMillis() / 1000) / mpro.tickSize
	          if (l.length == 0) {
	            l.+=:(new OHLC(t.unitPrice, t.unitPrice, t.unitPrice, t.unitPrice, 
			           t.amount, new DateTime(time) , new Duration(mpro.tickSize*1000)))
	          } else {
	           var myOhlc = updateGivenOHLC(l.head, t)
	           if(l.head.date.getMillis()/1000 == time ) {
	             l.update(0, myOhlc)
	           } else {
	             l.+=:(myOhlc)
	           }
	          }
	        }
	      }
        }) 
      }
    }
    
    
    private def updateGivenOHLC(o: OHLC, t: Transaction): OHLC = {
       var open = o.open
	   var high = if(o.high > t.unitPrice) o.high else t.unitPrice
	   var low = if(o.low < t.unitPrice) o.low else t.unitPrice
	   var close = t.unitPrice
	   var volume = o.volume + t.amount
	   var date = o.date
	   var duration = o.duration
	   return new OHLC(open, high, low, close, volume, date, duration)
    }
    
    
    
    
    private def creatOhlcForMpro(mpro: MarketPairRegistrationOHLC) {
      var thelist = new MutableList[OHLC]()
      trans.get(new MarketPairRegistrationTransaction(mpro.market, mpro.c)) match {
        case None => new MutableList()
        case Some(t) => t.map(e => {
          var time = (e.timestamp.getMillis() / 1000) / mpro.tickSize 
          if(thelist.length == 0) {
            thelist += new OHLC(e.unitPrice, e.unitPrice, e.unitPrice, e.unitPrice, 
		           e.amount, new DateTime(time) , new Duration(mpro.tickSize*1000))
          } else {
	          var head = thelist.head
	           if(head.date.getMillis() == time) {
	            thelist.update(0, updateGivenOHLC(head, e))
	          } else {
	            thelist.+=:(new OHLC(e.unitPrice, e.unitPrice, e.unitPrice, e.unitPrice, 
			           e.amount, new DateTime(time) , new Duration(mpro.tickSize*1000)))
	          }
          }
        }
      )}
      ohlc += (mpro -> thelist)
      
    }
      
    
    
    def getLatestOhlc(mpro: MarketPairRegistrationOHLC) = {
      ohlc.get(mpro) match {
        case None => new OHLC(0.0, 0.0, 0.0, 0.0, 0.0, new DateTime() , new Duration)
        case Some(l) => l.head
      }
    }

    def getAllTransByMarketPair(mp : MarketPair) = {
      trans.get(new MarketPairRegistrationTransaction(mp.market, mp.c)) match {
        case None => new MutableList[Transaction]
        case Some(l) => l;
      }
    }
    
    
  }