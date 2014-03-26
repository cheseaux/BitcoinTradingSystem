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
    var inst = new BtceAPI(Currency.BTC, Currency.USD);
    println(inst.getTrade(3));
  }

  
  
}
