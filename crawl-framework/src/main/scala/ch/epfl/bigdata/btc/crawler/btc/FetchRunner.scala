package ch.epfl.bigdata.btc.crawler.btc

import ch.epfl.bigdata.btc.crawler.coins.DataSource
import ch.epfl.bigdata.btc.crawler.twitter.TwitterActor
import akka.actor.{ ActorSystem, Props }

object FetchRunner extends App {
  val system = ActorSystem("DataSourceSystem")
  
  ActorPool.twitterFetcher ! "start"
}

object ActorPool {
  val dataSource = FetchRunner.system.actorOf(Props[DataSource], "DataSource")
  val twitterFetcher = FetchRunner.system.actorOf(Props(classOf[TwitterActor], dataSource), "tweetFetcher")
}
