package ch.epfl.bigdata.btc.crawler.coins.types

import Currency._
import OfferType._
import com.github.nscala_time.time.Imports._

abstract class AbstractOffer(	from: Currency,			// The currency you want to use to buy what
								to: Currency,			// The currency you want to buy (BTC, LTC,...)
								unitPrice: Double,		// The unit price of what currency in with currency 
								amount: Double,		// The amount of the what currency you want to buy
								timestamp: DateTime,		// When this Offer was detected
								direction: OfferType ) {
  
}

case class Transaction(	from: Currency,			// The currency you want to use to buy what
					to: Currency,			// The currency you want to buy (BTC, LTC,...)
					unitPrice: Double,		// The unit price of what currency in with currency 
					amount: Double,		// The amount of the what currency you want to buy
					tradeId: Int,
					timestamp: DateTime,		// When this Offer was detected
					direction: OfferType) 
					extends AbstractOffer(from, to, unitPrice, amount, timestamp, direction) {
  

  
  
}

case class BitstampCaseTransaction(date: String, tid: Int, price: String, 
    amount: String)

case class BitfinexCaseTransaction(timestamp: Long, tid: Int, price: String, 
    amount: String, exchange: String)
    
case class BTCeCaseTransaction(date: Long, price: Double, amount: Double, 
    tid: Int, price_currency: String, item: String, trade_type: String)
    
    
    
    
    
    
    
    