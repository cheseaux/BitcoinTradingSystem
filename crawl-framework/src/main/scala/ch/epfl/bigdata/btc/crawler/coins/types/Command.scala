package ch.epfl.bigdata.btc.crawler.coins.types

import ch.epfl.bigdata.btc.crawler.coins.types.Market._

sealed trait Command
case object CommandStop extends Command
case object CommandFetch extends Command

case class MarketPair(market: Market, c: CurrencyPair)

// Registration objects
case class MarketPairRegistrationOHLC(market: Market, c: CurrencyPair, tickSize: Int, tickCount: Int)
case class MarketPairRegistrationTransaction(market: Market, c: CurrencyPair)
case class TwitterRegistrationFull()