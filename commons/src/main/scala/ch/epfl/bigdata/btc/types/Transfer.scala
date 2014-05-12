package ch.epfl.bigdata.btc.types

import Indicator._
import Currency._
import OfferType._
import Market._
import com.github.nscala_time.time.Imports._

object Transfer {
case class Transaction( from: Currency,			// The currency you want to use to buy what
						to: Currency,			// The currency you want to buy (BTC, LTC,...)
						unitPrice: Double,		// The unit price of what currency in with currency 
						amount: Double,			// The amount of the what currency you want to buy
						tradeId: Int,
						timestamp: DateTime,	// When this Offer was detected
						direction: OfferType,
						market: Market)
						
						
						
						


case class OHLC (open: Double, high: Double, low: Double, close: Double, 
    volume: Double, date: DateTime, duration: Duration)
    
case class Tweet(date: DateTime, content: String, sentiment: Int)

case class Points(ind: Indicator, values: List[Tuple2[Double,Long]])
}