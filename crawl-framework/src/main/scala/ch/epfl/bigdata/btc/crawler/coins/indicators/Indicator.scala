package ch.epfl.bigdata.btc.crawler.coins.indicators

import akka.actor.{ActorSystem, ActorLogging, Actor, Props, ActorRef}

import ch.epfl.bigdata.btc.crawler.coins.types.OHLC
import ch.epfl.bigdata.btc.crawler.coins.types.Market._
import ch.epfl.bigdata.btc.crawler.coins.types._
import scala.collection.mutable.MutableList
import ch.epfl.bigdata.btc.crawler.coins.DataSourceFactory

abstract class Indicator(marketPool: ActorRef, watched: List[MarketPair]) extends Actor {
  
	var ticks: MutableList[OHLC] = new MutableList[OHLC]()
	watched.map(f => marketPool ! MarketPairRegistration(f.market, f.c1, f.c2))
	
	def receive = {
	  case t: OHLC => updateTicks(t)
  	}
	
	
	def updateTicks(tick: OHLC) = {
	  if (ticks.isEmpty) {
	    ticks += tick
	  } else {
	    /*ActorPool
	    val last  = ticks.last
	    val length = ticks.length
	    if (last.date equals tick.date) {
	      ticks.update(length, tick)
	    } else {
	      ticks += tick
	      if (length > tickCount) {
	        ticks = ticks.drop(1)
	      }
	    }*/
	  }
	}
	
	def recompute()
	
	def getValues()
	
	


}