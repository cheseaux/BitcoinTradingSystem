package ch.epfl.bigdata.btc.types



object Indicator extends Enumeration {
	type Indicator = Value
	val EMA = Value("ema")
	val SMA = Value("sma")
}