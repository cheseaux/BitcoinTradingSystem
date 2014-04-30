package ch.epfl.bigdata.btc.crawler.coins.types

sealed trait Command
case object CommandStop extends Command
case object CommandFetch extends Command
