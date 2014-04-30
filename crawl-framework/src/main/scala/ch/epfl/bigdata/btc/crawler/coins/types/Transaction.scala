package ch.epfl.bigdata.btc.crawler.coins.types

case class BitstampCaseTransaction(date: String, tid: Int, price: String, 
    amount: String)

case class BitfinexCaseTransaction(timestamp: Long, tid: Int, price: String, 
    amount: String, exchange: String)
    
case class BTCeCaseTransaction(date: Long, price: Double, amount: Double, 
    tid: Int, price_currency: String, item: String, trade_type: String)
    
    
    
    
    
    
    
    