package ch.epfl.bigdata.btc.crawler.btc

import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.crawler.coins.types._

import akka.actor.{ActorSystem, Props}

object FetchRunner extends App {
	val system = ActorSystem("mySystem")
	val myActor = system.actorOf(Props[MarketFetchPool], "myactor2")
	
	myActor ! new MarketPairRegistration(Market.BTCe, Currency.USD, Currency.BTC)
}