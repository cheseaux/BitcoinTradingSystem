package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator
import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.CurrencyPair
import scala.collection.mutable.MutableList
import scala.collection.mutable.HashMap


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
        case _ =>
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
    
    
    def getAllTransByMarketPair(mp : MarketPair) = {
      trans.get(new MarketPairRegistrationTransaction(mp.market, mp.c)) match {
        case None => new MutableList[Transaction]
        case Some(l) => l;
      }
    }
    
    
  }