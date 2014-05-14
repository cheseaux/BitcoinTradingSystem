
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

class CacheTest extends AssertionsForJUnit {

  var cache = new Cache()

  @Before def initialize() {
    
  }

  @Test def verifyEasy() {
    var reg = new MarketPairRegistrationOHLC(Market.BTCe, new CurrencyPair(Currency.BTC, Currency.USD), 30, 26)
    var ohlc1 = new OHLC(1,1,1,1,1, new DateTime(150000), new Duration(30000))
    var ohlc2 = new OHLC(2,2,2,2,2, new DateTime(210000), new Duration(30000))
    var ohlc3 = new OHLC(3,3,3,3,3, new DateTime(90000), new Duration(30000))
    var ohlc4 = new OHLC(4,4,4,4,4, new DateTime(270000), new Duration(30000))
    var ohlc5 = new OHLC(5,5,5,5,5, new DateTime(150000), new Duration(30000))
    
    cache.addOhlcType(reg)
    
    cache.updateOhlcForMpro(reg, ohlc1)
    
    cache.updateOhlcForMpro(reg, ohlc3)
    //println(cache.getLatestOhlc(reg))
    
    cache.updateOhlcForMpro(reg, ohlc2)
    //println(cache.getLatestOhlc(reg))
    cache.updateOhlcForMpro(reg, ohlc4)
    
    cache.updateOhlcForMpro(reg, ohlc5)
    
    //println(cache.getLatestOhlc(reg))
    
    println(cache.getAllOhlc(reg))
    
    
  }

  
  
}
