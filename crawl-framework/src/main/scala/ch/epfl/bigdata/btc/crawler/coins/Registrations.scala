package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator
import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.CurrencyPair
import scala.collection.mutable.MutableList
import scala.collection.mutable.HashMap
import akka.actor.{Actor, ActorRef, Props}

 /**
   * This is the internal observer store
   */
  class Registrations {
	  
      val indicators = new MutableList[IndicatorRegistration]()
    
	  val twitter = new MutableList[ActorRef]()
	  val ohlc = new HashMap[MarketPair, MutableList[ActorRef]]()
	  val trans = new HashMap[MarketPair, MutableList[ActorRef]]()
	  
	  val actorsOhlc = new HashMap[ActorRef, MutableList[MarketPairRegistrationOHLC]]()
	  val actorsTrans = new HashMap[ActorRef, MutableList[MarketPair]]()
	  
	  def getTwitterRegistrations() = twitter
	  def getIndicatorRegistrations() = indicators
	  def getOhlcRegByMarketPair(mp : MarketPair) = ohlc.get(mp)
	  def getTransRegByMarketPair(mp : MarketPair) = trans.get(mp)
	  
	  def getOhlcRegByActor(a : ActorRef) = actorsOhlc.get(a)
	  def getTransRegByActor(a : ActorRef) = actorsTrans.get(a)
	  
	  def addTwitterRegistration(a: ActorRef) {
	    twitter += a
	  }
	  def addOhlcRegistration(mpro : MarketPairRegistrationOHLC, a: ActorRef) {
	    val mp = new MarketPair(mpro.market, mpro.c)
	    ohlc.get(mp) match {
	     case None => ohlc += (mp -> ((new MutableList[ActorRef]()) += a))
	     case Some(m) => m += a
	    }
	    actorsOhlc.get(a) match {
	     case None => actorsOhlc += (a -> ((new MutableList[MarketPairRegistrationOHLC]()) += mpro))
	     case Some(m) => m += mpro
	    }
	  }
	  def addTransRegistration(mp : MarketPair, a: ActorRef) {
	    trans.get(mp) match {
	     case None => trans += (mp -> ((new MutableList[ActorRef]()) += a))
	     case Some(m) => m += a
	    }
	    actorsTrans.get(a) match {
	     case None => actorsTrans += (a -> ((new MutableList[MarketPair]()) += mp))
	     case Some(m) => m += mp
	    }
	  }
	  
	  def addIndicator(i: IndicatorRegistration) {
	    if(!indicators.contains(i)) {
	      indicators += i;
	    }
	  }
  }