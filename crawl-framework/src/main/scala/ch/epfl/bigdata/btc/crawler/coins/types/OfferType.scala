package ch.epfl.bigdata.btc.crawler.coins.types

object OfferType extends Enumeration {
	type OfferType = Value
	val BID = Value("bid")
	val ASK = Value("ask")
}