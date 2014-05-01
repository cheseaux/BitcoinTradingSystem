package ch.epfl.bigdata.btc.crawler.twitter

import twitter4j._

object Twitter {

	val config = new twitter4j.conf.ConfigurationBuilder()
	.setOAuthConsumerKey("h7HL6oGtIOrCZN53TbWafg")
	.setOAuthConsumerSecret("irg8l38K4DUrqPV638dIfXvK0UjVHKC936IxbaTmqg")
	.setOAuthAccessToken("77774972-eRxDxN3hPfTYgzdVx99k2ZvFjHnRxqEYykD0nQxib")
	.setOAuthAccessTokenSecret("FjI4STStCRFLjZYhRZWzwTaiQnZ7CZ9Zrm831KUWTNZri")
	.build

	def simpleStatusListener = new StatusListener() {
		def onStatus(status: Status) { 
		  println("["+status.getCreatedAt()+"]" + status.getText().replace('\n', ' '))
		}
		def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
		def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
		def onException(ex: Exception) { ex.printStackTrace }
		def onScrubGeo(arg0: Long, arg1: Long) {}
		def onStallWarning(warning: StallWarning) {}
	}

	def main(args: Array[String]) {
	  // get ref on GUI actor
//	  val myRemoteActor = akka.actor(Node("myMachine", 8000), 'anActor)
//	  127.0.0.1
//	  akka.actor.ActorSelection("akka://application/user/$b/BTC")
//	  akka.context.select()
	  
		val twitterStream = new TwitterStreamFactory(config).getInstance
				twitterStream.addListener(simpleStatusListener)
				twitterStream.filter(new FilterQuery().track(Array("bitcoin","btc","bitcoins", "BTC", "cryptocurrency")))
	}

}