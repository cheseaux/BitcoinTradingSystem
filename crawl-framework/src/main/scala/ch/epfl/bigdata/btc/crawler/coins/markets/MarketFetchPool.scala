package ch.epfl.bigdata.btc.crawler.coins.markets

import ch.epfl.bigdata.btc.crawler.coins.types.Market._
import ch.epfl.bigdata.btc.crawler.coins.types.Currency._
import ch.epfl.bigdata.btc.crawler.coins.types._
import ch.epfl.bigdata.btc.crawler.coins.DataSource


import scala.concurrent.duration._
import collection.mutable.HashMap
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import akka.event.Logging


class MarketFetchPool(dataSource: ActorRef) extends Actor {
  import context._
  var fetchers: HashMap[Market, HashMap[CurrencyPair, ActorRef]] = new HashMap;
  
  
  def receive() = {
    case t : Transaction => sendToDataSource(t)
    case r : MarketPairRegistration => register(r.market, r.c)
  }
  

  def register(market: Market, c: CurrencyPair) {
    addFetcher(market, c)
  }
  
  
  private def addFetcher(market: Market, c: CurrencyPair) {
    fetchers.get(market)  match {
      case None => addMarket(market, c)
      case Some(m) => addPair(m, market, c)
    }
  }
  
  private def addMarket(market: Market, c: CurrencyPair) {
    val m = new HashMap[CurrencyPair, ActorRef]()
    addPair(m, market, c)
    fetchers += (market -> m)
  }
  
  private def addPair(m: HashMap[CurrencyPair, ActorRef], market: Market, c: CurrencyPair){
    if (!m.contains(c)) {
	    val f = createFetcher(market, c.c1, c.c2)
	    m += (c -> f)
	    scheduleFetcher(f)
    }
  }
  
  private def createFetcher(market: Market, c1: Currency, c2: Currency) : ActorRef = market match {
    case BTCe => context.actorOf(Props(classOf[BtcePublicFetcher], c1, c2), "BTC-e_" + c1 + "-" + c2)
  }
  
  private def scheduleFetcher(f: ActorRef) {
    system.scheduler.schedule(0 seconds, 12 seconds, f, CommandFetch)
  }
  
  private def sendToDataSource(t: Transaction) {
    println(t)
    dataSource ! t
  }
  
}