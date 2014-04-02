package ch.epfl.bigdata.btc.crawler.coins.types

import ch.epfl.bigdata.btc.crawler.coins.types.Market._
import ch.epfl.bigdata.btc.crawler.coins.types.Currency._

sealed trait Command
case object CommandStop extends Command
case object CommandFetch extends Command

case class MarketPairRegistration(market: Market, c1: Currency, c2: Currency)