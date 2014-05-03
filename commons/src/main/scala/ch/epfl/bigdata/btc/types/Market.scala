package ch.epfl.bigdata.btc.types

object Market extends Enumeration {
	type Market = Value
	val BTCe = Value("btce")
	val Bitstamp = Value("bitstamp")
	val Bitfinex = Value("bitfinex")
}