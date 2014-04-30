package ch.epfl.bigdata.btc.types

import ch.epfl.bigdata.btc.types.Currency._

case class CurrencyPair(c1: Currency, c2: Currency) {
  override def equals(o: Any) = o match {
    case that: CurrencyPair => ((c1 == that.c1 && c2 == that.c2) || (c1 == that.c2 && c2 == that.c1))
    case _ => false
  }
  override def hashCode = {c1.hashCode() + c2.hashCode()}
}