package ch.epfl.bigdata.btc.crawler.coins.markets


import akka.actor.{Actor, ActorRef}
import com.github.nscala_time.time.Imports._
import ch.epfl.bigdata.btc.crawler.coins.types.{CommandFetch, CommandStop, Transaction, OfferType}
import ch.epfl.bigdata.btc.crawler.coins.types.Currency._
import ch.epfl.bigdata.btc.crawler.coins.types.Market._
import ch.epfl.bigdata.btc.crawler.coins.types.Market

abstract class PublicFetchActor extends Actor with PublicFetcher  {
  
  def fetch()
  def exit()
  
  def receive = {
	case CommandFetch => fetch()
	case CommandStop => exit()
  }
  
  protected def sendResults(t: List[Transaction]) {
    //println(t)
    t.map(e => sender ! e)
  }
}


final class BtcePublicFetcher(c1: Currency, c2: Currency) extends PublicFetchActor {
  val btce = new BtceAPI(c1, c2)
  var count = 2000
  var latest = new Transaction(c1, c2, 0.0, 0.0, 0, new DateTime, OfferType.BID, Market.BTCe)
  
  def fetch() {
    println("fetch called");
    val trades = btce.getTrade(count)
    val idx = trades.indexOf(latest)
    count = if (idx < 0)  2000 else Math.min(10*idx, 2000)
    latest = trades.head
    
    if(idx > 0)
    	sendResults(trades.slice(0, idx))
    else if (idx == -1)
    	sendResults(trades)
  }
  
  def exit() {}
}