package ch.epfl.bigdata.btc.crawler.btc

import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool

import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.CurrencyPair
import ch.epfl.bigdata.btc.types.Market
import ch.epfl.bigdata.btc.types.Currency
import ch.epfl.bigdata.btc.crawler.coins.types._
import ch.epfl.bigdata.btc.crawler.coins.DataSource
import ch.epfl.bigdata.btc.crawler.twitter.TwitterActor
import akka.actor.{ ActorSystem, Props }

object FetchRunner extends App {
  val system = ActorSystem("DataSourceSystem")

  println(system.name)

  // twittactor ! begin
  //ActorPool.twitterFetcher ! "start"

  ActorPool.dataSource ! new MarketPairRegistrationTransaction(
    Market.BTCe, CurrencyPair(Currency.USD, Currency.BTC))

  println("FetchRunner | dataSource")

  ActorPool.dataSource ! new MarketPairRegistrationTransaction(
    Market.BTCe, CurrencyPair(Currency.BTC, Currency.USD))
  
  ActorPool.dataSource ! new EMARegistration(
    Market.BTCe, CurrencyPair(Currency.BTC, Currency.USD), 30, 26)

  println("FetchRunner | dataSource")

}

object ActorPool {
  val dataSource = FetchRunner.system.actorOf(Props[DataSource], "DataSource")
  val twitterFetcher = FetchRunner.system.actorOf(Props(classOf[TwitterActor], dataSource), "tweetFetcher")
}


