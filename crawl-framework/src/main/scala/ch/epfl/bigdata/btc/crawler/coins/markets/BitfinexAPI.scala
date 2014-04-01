package ch.epfl.bigdata.btc.crawler.coins.markets

import org.apache.http.client.fluent._
import net.liftweb.json._
import org.joda.time.DateTime
import ch.epfl.bigdata.btc.crawler.coins.types._
import ch.epfl.bigdata.btc.crawler.coins.types.Currency._




class BitfinexAPI(from: Currency, to: Currency){
  implicit val formats = net.liftweb.json.DefaultFormats
  
  val serverBase = "https://api.bitfinex.com/v1/"
  val pair = pair2path
  
	def getInfo() {
    
	}
	
	def getTicker() {
	  
	}
	
	def getTrade(count: Int) : List[Transaction] = {
	  var path = serverBase + "/trades/" + pair
	  var json = Request.Get(path).execute().returnContent().asString()
	  
	  var t = parse(json).extract[List[BitfinexCaseTransaction]]
	  
	  return t.map(f => new Transaction( from, to, f.price.toDouble, 
	      f.amount.toDouble, f.tid, new DateTime(f.timestamp), OfferType.BID))
	}
	
	
	def getDepth() {
	  
	}
	
	
	private def pair2path() = from match {
	  case Currency.BTC => to match {
	    case Currency.USD => "btcusd"
	  }
	  case Currency.USD => to match {
	    case Currency.BTC => "btcusd"
	  }
	}
}