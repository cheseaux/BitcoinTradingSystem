
package ch.epfl.bigdata.test.btc.crawler.coins

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before
import ch.epfl.bigdata.btc.crawler.coins.Cache
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Market
import ch.epfl.bigdata.btc.types.Currency
import ch.epfl.bigdata.btc.types.CurrencyPair
import org.joda.time.DateTime
import org.joda.time.Duration
import ch.epfl.bigdata.btc.types.OfferType

class CacheTest extends AssertionsForJUnit {

  var cache = new Cache()

  @Before def initialize() {
    
  }

  @Test def verifyEasy() {
    var reg = new MarketPairRegistrationOHLC(Market.BTCe, new CurrencyPair(Currency.BTC, Currency.USD), 30, 26)
    var mp =  new MarketPair(Market.BTCe, new CurrencyPair(Currency.BTC, Currency.USD))
    
    var ohlc1 = new OHLC(1,1,1,1,1, new DateTime(150000), new Duration(30000))
    var ohlc2 = new OHLC(2,2,2,2,2, new DateTime(210000), new Duration(30000))
    var ohlc3 = new OHLC(3,3,3,3,3, new DateTime(90000), new Duration(30000))
    var ohlc4 = new OHLC(4,4,4,4,4, new DateTime(270000), new Duration(30000))
    var ohlc5 = new OHLC(5,5,5,5,5, new DateTime(150000), new Duration(30000))
    
    var trans1 = new Transaction(Currency.BTC, Currency.USD, 0.1, 0.1, 1, new DateTime(150000), OfferType.ASK,Market.BTCe)
    var trans2 = new Transaction(Currency.BTC, Currency.USD, 0.2, 0.2, 2, new DateTime(210000), OfferType.ASK,Market.BTCe)
    var trans3 = new Transaction(Currency.BTC, Currency.USD, 0.3, 0.3, 3, new DateTime(90000), OfferType.ASK,Market.BTCe)
    var trans4 = new Transaction(Currency.BTC, Currency.USD, 0.4, 0.4, 4, new DateTime(270000), OfferType.ASK,Market.BTCe)
    var trans5 = new Transaction(Currency.BTC, Currency.USD, 0.5, 0.5, 5, new DateTime(150000), OfferType.ASK,Market.BTCe)
    var trans6 = new Transaction(Currency.BTC, Currency.USD, 0.6, 0.6, 6, new DateTime(610000), OfferType.ASK,Market.BTCe)

    println("\n\n\n\n\n\n\n---------------------")
    cache.addOhlcType(reg)
   
    println("\n\n\n\n\n\n\n---------------------")
    cache.updateOHLC(mp, trans2)
    if (cache.getAllOhlc(reg).length != 1) {
      fail()
    }
    
    
    println("\n\n\n\n\n\n\n---------------------")
    cache.updateOHLC(mp, trans3)
    if (cache.getAllOhlc(reg).length != 5) {
      fail()
    }
    
    if (cache.getAllOhlc(reg).head.volume != 0.3) {
      fail()
    }
    
    println("\n\n\n\n\n\n\n---------------------")
    cache.updateOHLC(mp, trans1)
    if (cache.getAllOhlc(reg).length != 5) {
      fail()
    }
    
    cache.updateOHLC(mp, trans1)
    if (cache.getAllOhlc(reg).length != 5) {
      fail()
    }
    
    if (cache.getAllOhlc(reg).head.volume != 0.3) {
      fail()
    }
    
    if (cache.getAllOhlc(reg).last.volume != 0.2) {
      fail()
    }
    
    println("\n\n\n\n\n\n\n---------------------")
    cache.updateOHLC(mp, trans6)
    println("\n\n\n\n\n\n\n---------------------")
    cache.updateOHLC(mp, trans6)
    println("\n\n\n\n\n\n\n---------------------")
    cache.updateOHLC(mp, trans5)
    println("\n\n\n\n\n\n\n---------------------")
    cache.updateOHLC(mp, trans5)
    println("\n\n\n\n\n\n\n---------------------")
    
    var current = new DateTime(90000)
    var l = cache.getAllOhlc(reg)
 
    println(l)
    
    for(i <- 0 to l.length -1) {
      l.get(i) match {
        case Some(n) => {
          println( n)
          if (!n.date.equals(current)) fail()
          current = current.plus(30000)
        }
        case None => fail()
      }
    }
    
    
    println("\n\n\n\n\n\n")
    
    current = new DateTime(90000)
    for(i <- 0 to l.length -1) {
      var r = cache.getOhlcByTimestampAndMpro(reg, current)
    		  println(r)
      current = current.plus(30000)
       
    }
    
    
    
 
    
  }

  
  
}
