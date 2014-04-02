package ch.epfl.bigdata.btc.crawler.coins.markets

import ch.epfl.bigdata.btc.crawler.coins.types.Market._
import ch.epfl.bigdata.btc.crawler.coins.types.Currency._
import ch.epfl.bigdata.btc.crawler.coins.types._


import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class MarketFetchPool extends Actor {
  import context._
  var fetchers: Map[Market, Map[Tuple2[Currency, Currency], ActorRef]] 
    = new collection.immutable.HashMap;
  
  
  def receive() = {
    case x : Transaction => println(x.amount + "@" + x.unitPrice + " at " + x.timestamp )
    case r : MarketPairRegistration => register(r.market, r.c1, r.c2)
  }
  

  def register(market: Market, c1: Currency, c2: Currency) {
    val f = createFetcher(market, c1, c2)
    addFetcher(f, market, c1, c2)
    scheduleFetcher(f)
  }
  
  
  private def addFetcher(f: ActorRef, market: Market, c1: Currency, c2: Currency) {
    if (fetchers.contains(market)) {
      val marketFetchers = fetchers.get(market)
        .asInstanceOf[Map[Tuple2[Currency, Currency], ActorRef]]
      if(!(marketFetchers.contains(new Tuple2(c1, c2)) || 
          marketFetchers.contains(new Tuple2(c2, c1)))) {
        val m = marketFetchers + (new Tuple2(c1, c2) -> f)
        fetchers += (market -> m)
      }
    } else {
      val m = collection.immutable.HashMap(new Tuple2(c1, c2) -> f)
      fetchers += (market -> m)
    }
  }
  private def scheduleFetcher(f: ActorRef) {
    system.scheduler.schedule(0 seconds, 12 seconds, f, CommandFetch)
  }
  private def createFetcher(market: Market, c1: Currency, c2: Currency) : ActorRef = market match{
    case BTCe => context.actorOf(Props(classOf[BtcePublicFetcher], c1, c2), "BTC-e_" + c1 + "-" + c2)
  }
  
}