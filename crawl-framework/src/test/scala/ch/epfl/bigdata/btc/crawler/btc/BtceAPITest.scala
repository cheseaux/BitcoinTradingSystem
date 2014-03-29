package ch.epfl.bigdata.btc.crawler.btc

import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.Matchers._
import scala.collection.mutable.ListBuffer
import org.junit.Assert._
import org.junit.Test
import org.junit.Before
import ch.epfl.bigdata.btc.crawler.btc._

class BtceAPITest extends AssertionsForJUnit {

  var sb: StringBuilder = _
  var lb: ListBuffer[String] = _

  @Before def initialize() {
    sb = new StringBuilder("ScalaTest is ")
    lb = new ListBuffer[String]
  }

  @Test def verifyEasy() { // Uses JUnit-style assertions
    var inst0 = new BtceAPI(Currency.BTC, Currency.USD)
    println("BtceAPI: \t" + inst0.getTrade(4).slice(0, 3))
    var inst1 = new BitfinexAPI(Currency.BTC, Currency.USD)
    println("BitfinexAPI: \t" + inst1.getTrade(3).slice(0, 3))
    var inst2 = new BitstampAPI(Currency.BTC, Currency.USD)
    println("BitstampAPI: \t" + inst2.getTrade(3).slice(0, 3))
  }

  
  
}
