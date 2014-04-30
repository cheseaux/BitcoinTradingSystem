package ch.epfl.bigdata.btc.crawler.coins.markets

import org.apache.http.client.fluent._
import net.liftweb.json._
import org.joda.time.DateTime

import ch.epfl.bigdata.btc.types._
import ch.epfl.bigdata.btc.types.Currency._
import ch.epfl.bigdata.btc.types.Market

import ch.epfl.bigdata.btc.types.Transfer._

import ch.epfl.bigdata.btc.crawler.coins.types._
import ch.epfl.bigdata.btc.types.OfferType





class BitstampAPI(from: Currency, to: Currency) {
  implicit val formats = net.liftweb.json.DefaultFormats
  
  val serverBase = "https://www.bitstamp.net/api/transactions/"
 
	def getInfo() {
    
	}
	
	def getTicker() {
	  
	}
	
	def getTrade(count: Int) : List[Transaction] = {
	  var path = serverBase
	  var json = Request.Get(path).execute().returnContent().asString()
	  
	  var t = parse(json).extract[List[BitstampCaseTransaction]]

	  return t.map(f => new Transaction(from, to, f.price.toDouble, 
	      f.amount.toDouble, f.tid, new DateTime(f.date.toLong), OfferType.BID, Market.Bitstamp))
	}
	
	def getDepth() {
	  
	}
}