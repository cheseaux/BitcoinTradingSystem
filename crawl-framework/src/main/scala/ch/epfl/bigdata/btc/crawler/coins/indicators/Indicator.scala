package ch.epfl.bigdata.btc.crawler.coins.indicators

import akka.actor.{ActorSystem, ActorLogging, Actor, Props}

import ch.epfl.bigdata.btc.crawler.coins.types.OHLC
import scala.collection.mutable.MutableList
import ch.epfl.bigdata.btc.crawler.coins.DataSourceFactory

abstract class Indicator(tickSize: Int, tickCount: Int) extends Actor {
  
	var ticks: MutableList[OHLC] = new MutableList[OHLC]()
	DataSourceFactory.getDataSource.register(this, tickSize, tickCount)
	
	def receive = {
	  case OHLC => println("received")
  	}
	
	
	def updateTicks(tick: OHLC) = {
	  if (ticks.isEmpty) {
	    ticks += tick
	  } else {
	    val last  = ticks.last
	    val length = ticks.length
	    if (last.date equals tick.date) {
	      ticks.update(length, tick)
	    } else {
	      ticks += tick
	      if (length > tickCount) {
	        ticks = ticks.drop(1)
	      }
	    }
	  }
	}
	
	def recompute()
	
	def getValues()
	
	


}