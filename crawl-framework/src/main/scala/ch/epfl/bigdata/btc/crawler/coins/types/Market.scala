package ch.epfl.bigdata.btc.crawler.coins.types

object Market extends Enumeration {
	type Market = Value
	val BTCe = Value("btce")
	val Bitstamp = Value("bitstamp")
	val Bitfinex = Value("bitfinex")
}