package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator

import scala.collection.mutable.MutableList


object DataSource {
  
  var observers: MutableList[Tuple3[Indicator, Int, Int]] 
    = new MutableList[Tuple3[Indicator, Int, Int]]();
  
	def register(indicator: Indicator, tickSize: Int, tickCount: Int) {
	  observers += new Tuple3(indicator, tickSize, tickCount);
	}
}