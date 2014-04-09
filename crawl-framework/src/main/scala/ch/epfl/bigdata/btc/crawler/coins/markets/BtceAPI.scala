package ch.epfl.bigdata.btc.crawler.coins.markets

import org.apache.http.client.fluent._
import net.liftweb.json._
import org.joda.time.DateTime

import ch.epfl.bigdata.btc.crawler.coins.types._
import ch.epfl.bigdata.btc.crawler.coins.types.Currency._
import ch.epfl.bigdata.btc.crawler.coins.types.Market._






class BtceAPI(from: Currency, to: Currency) {
  implicit val formats = net.liftweb.json.DefaultFormats
  
  val serverBase = "https://btc-e.com/api/2/"
  val pair = pair2path
  
	def getInfo() {
    
	}
	
	def getTicker() {
	  
	}
	
	def getTrade(count: Int) : List[Transaction] = {
	  var path = serverBase + pair + "/trades/" + count
	  var json = Request.Get(path).execute().returnContent().asString()
	  
	  var t = parse(json).extract[List[BTCeCaseTransaction]]
	  
	  return t.map(f => new Transaction(
	      Currency.withName(f.price_currency.toLowerCase()),
	      Currency.withName(f.item.toLowerCase()), f.price, f.amount, f.tid,
	      new DateTime(f.date*1000), OfferType.withName(f.trade_type), Market.BTCe))
	}
	
	def getDepth() {
	  
	}
	
	
	private def pair2path() = from match {
	  case Currency.BTC => to match {
	    case Currency.USD => "btc_usd"
	  }
	  case Currency.USD => to match {
	    case Currency.BTC => "btc_usd"
	  }
	}
}