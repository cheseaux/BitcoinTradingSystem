package ch.epfl.bigdata.btc.crawler.coins.indicators

import akka.actor.{ActorSystem, ActorLogging, Actor, Props, ActorRef}

import ch.epfl.bigdata.btc.crawler.coins.types.OHLC
import ch.epfl.bigdata.btc.crawler.coins.types.Market._
import ch.epfl.bigdata.btc.crawler.coins.types._
import scala.collection.mutable.MutableList

abstract class Indicator(dataSource: ActorRef, watched: MarketPairRegistrationOHLC) extends Actor {
  
	var ticks: MutableList[OHLC] = new MutableList[OHLC]()
	dataSource ! watched
	
	def receive = {
	  case t: OHLC => updateTicks(t)
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
	      if (length > watched.tickCount) {
	        ticks = ticks.drop(1)
	      }
	    }
	  }
	}
	
	def recompute()
}