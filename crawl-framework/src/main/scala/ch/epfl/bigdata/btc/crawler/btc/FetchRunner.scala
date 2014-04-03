package ch.epfl.bigdata.btc.crawler.btc

import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.crawler.coins.types._

import ch.epfl.bigdata.btc.crawler.coins.DataSource

import akka.actor.{ActorSystem, Props}

object FetchRunner extends App {
	val system = ActorSystem("mySystem")
	
	println(system.name)
	
	ActorPool.marketFetchPool ! new MarketPairRegistration(
	    Market.BTCe, CurrencyPair(Currency.USD, Currency.BTC))
	
	ActorPool.marketFetchPool ! new MarketPairRegistration(
	    Market.BTCe, CurrencyPair(Currency.BTC, Currency.USD))
	
}

object ActorPool {
  val dataSource = FetchRunner.system.actorOf(Props[DataSource], "DataSource")
  val marketFetchPool = FetchRunner.system.actorOf(Props(classOf[MarketFetchPool], dataSource), "MarketFetchPool")
  
}

