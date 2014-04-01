package ch.epfl.bigdata.btc.crawler.coins.types

import org.joda.time.DateTime
import org.joda.time.Duration

case class OHLC (open: Double, high: Double, low: Double, close: Double, 
    volume: Double, date: DateTime, duration: Duration)