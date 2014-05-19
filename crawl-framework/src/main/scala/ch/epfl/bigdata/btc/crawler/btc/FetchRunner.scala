package ch.epfl.bigdata.btc.crawler.btc

import ch.epfl.bigdata.btc.crawler.coins.DataSource
import ch.epfl.bigdata.btc.crawler.twitter.TwitterActor
import akka.actor.{ ActorSystem, Props }
import ch.epfl.bigdata.btc.crawler.coins.indicators.WalletSMA
import ch.epfl.bigdata.btc.types.Registration.SMARegistration
import ch.epfl.bigdata.btc.types.Market._
import ch.epfl.bigdata.btc.types.Market
import ch.epfl.bigdata.btc.types.CurrencyPair
import ch.epfl.bigdata.btc.types.Currency


object FetchRunner extends App {
  val system = ActorSystem("DataSourceSystem")
  
  ActorPool.twitterFetcher ! "start"
  
  
}

object ActorPool {
  val dataSource = FetchRunner.system.actorOf(Props[DataSource], "DataSource")
  val twitterFetcher = FetchRunner.system.actorOf(Props(classOf[TwitterActor], dataSource), "tweetFetcher")
  
   val wallet = FetchRunner.system.actorOf(Props(classOf[WalletSMA], dataSource, new SMARegistration(Market.Bitstamp, CurrencyPair(Currency.USD, Currency.BTC), 26, 100)), "WalletSMA")

}
