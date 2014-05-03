package ch.epfl.bigdata.btc.crawler.twitter

import twitter4j._
import akka.actor.ActorSelection
import akka.actor.Props
import akka.actor.Actor
import ch.epfl.bigdata.btc.crawler
import ch.epfl.bigdata.btc.crawler.btc.FetchRunner
import ch.epfl.bigdata.btc.crawler.coins.DataSource
import akka.actor.ActorRef
import ch.epfl.bigdata.btc.types.Transfer.Tweet
import org.joda.time.DateTime

class TwitterActor(dataSource: ActorRef) extends Actor {

  
  val config = new twitter4j.conf.ConfigurationBuilder()
    .setOAuthConsumerKey("h7HL6oGtIOrCZN53TbWafg")
    .setOAuthConsumerSecret("irg8l38K4DUrqPV638dIfXvK0UjVHKC936IxbaTmqg")
    .setOAuthAccessToken("77774972-eRxDxN3hPfTYgzdVx99k2ZvFjHnRxqEYykD0nQxib")
    .setOAuthAccessTokenSecret("FjI4STStCRFLjZYhRZWzwTaiQnZ7CZ9Zrm831KUWTNZri")
    .build

  def simpleStatusListener = new StatusListener() {
    def onStatus(status: Status) {
      println("[" + status.getCreatedAt() + "]" + status.getText())
      // send stuff to datasource
      
      dataSource ! new Tweet(new DateTime(status.getCreatedAt().getTime()), status.getText(), 0)

    }
    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
    def onException(ex: Exception) { ex.printStackTrace }
    def onScrubGeo(arg0: Long, arg1: Long) {}
    def onStallWarning(warning: StallWarning) {}
  }

  def receive() = {
    // DataSource receives a transaction from its fetchers.
    case "start" =>
      val twitterStream = new TwitterStreamFactory(config).getInstance
      twitterStream.addListener(simpleStatusListener)
      twitterStream.filter(new FilterQuery().track(Array("bitcoin")))

  }


}
