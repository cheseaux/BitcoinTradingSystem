package ch.epfl.bigdata.btc.crawler.btc

import ch.epfl.bigdata.btc.crawler.coins.DataSource
import ch.epfl.bigdata.btc.crawler.twitter.TwitterActor
import akka.actor.{ ActorSystem, Props }
import ch.epfl.bigdata.btc.crawler.coins.indicators.WalletSMA

object FetchRunner extends App {
  val system = ActorSystem("DataSourceSystem")
  
  ActorPool.twitterFetcher ! "start"
  
  
}

object ActorPool {
  val dataSource = FetchRunner.system.actorOf(Props[DataSource], "DataSource")
  val twitterFetcher = FetchRunner.system.actorOf(Props(classOf[TwitterActor], dataSource), "tweetFetcher")
  
   //val wallet = FetchRunner.system.actorOf(Props(classOf[WalletSMA], dataSource: ActorRef, watched: SMARegistration, msu : Double, btcsu : Double, 
   // maxInv : Double, maxbtc : Double), "WalletSMA")

}
