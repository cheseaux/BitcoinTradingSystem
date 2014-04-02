package ch.epfl.bigdata.btc.crawler.btc

import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.crawler.coins.types._

import akka.actor.{ActorSystem, Props}

object FetchRunner extends App {
	val system = ActorSystem("mySystem")
	
	ActorPool.marketFetchPool ! new MarketPairRegistration(Market.BTCe, Currency.USD, Currency.BTC)
}

object ActorPool {
  val marketFetchPool = FetchRunner.system.actorOf(Props[MarketFetchPool], "MarketFetchPool")
}

