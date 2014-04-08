package ch.epfl.bigdata.btc.crawler.twitter

import twitter4j._
import java.io.FileWriter
import java.util.Date

object TwitterLiveCrawler {

  /**
   * OAuth keys
   */
	val config = new twitter4j.conf.ConfigurationBuilder()
	.setOAuthConsumerKey("h7HL6oGtIOrCZN53TbWafg")
	.setOAuthConsumerSecret("irg8l38K4DUrqPV638dIfXvK0UjVHKC936IxbaTmqg")
	.setOAuthAccessToken("77774972-eRxDxN3hPfTYgzdVx99k2ZvFjHnRxqEYykD0nQxib")
	.setOAuthAccessTokenSecret("FjI4STStCRFLjZYhRZWzwTaiQnZ7CZ9Zrm831KUWTNZri")
	.build
	
	def toUnixTimeStamp(date : Date) : Long = {
	  return date.getTime() / 1000L;
	}

	def simpleStatusListener = new StatusListener() {
		def onStatus(status: Status) {
			val tweet = "["+toUnixTimeStamp(status.getCreatedAt())+"]["+status.getUser().getName()+"]" + status.getText()
			println(tweet)
			writeToFile(tweet +"\n")
		}
		def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
		def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {
			error("Track Limitation ! : " + numberOfLimitedStatuses)
		}
		def onException(ex: Exception) { ex.printStackTrace }
		def onScrubGeo(arg0: Long, arg1: Long) {}
		def onStallWarning(warning: StallWarning) {}
	}

	def writeToFile(str : String) {
		val fw = new FileWriter("live-tweets.txt", true)
		try {
			fw.write(str)
		}
		finally fw.close() 
	}

	def main(args: Array[String]) {

		val twitterStream = (new TwitterStreamFactory(config)).getInstance
				twitterStream.addListener(simpleStatusListener)
				twitterStream.filter(new FilterQuery().track(Array("#bitcoin")))
	}

}