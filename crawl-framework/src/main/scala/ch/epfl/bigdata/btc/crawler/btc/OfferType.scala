package ch.epfl.bigdata.btc.crawler.btc

import scala.Enumeration

object OfferType extends Enumeration {
	type OfferType = Value
	val BID = Value("bid")
	val ASK = Value("ask")
}