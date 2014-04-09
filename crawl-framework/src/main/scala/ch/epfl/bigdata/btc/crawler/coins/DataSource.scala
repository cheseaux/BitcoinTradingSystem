package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator
import ch.epfl.bigdata.btc.crawler.coins.markets.MarketFetchPool
import ch.epfl.bigdata.btc.crawler.coins.types.Transaction
import ch.epfl.bigdata.btc.crawler.coins.types.OHLC
import ch.epfl.bigdata.btc.crawler.coins.types.MarketPair
import ch.epfl.bigdata.btc.crawler.coins.types.MarketPairRegistration
import scala.collection.mutable.MutableList
import scala.collection.mutable.HashMap
import akka.actor.{Actor, ActorRef, Props}
import ch.epfl.bigdata.btc.crawler.coins.types.MarketPairRegistration


class DataSource(pool: ActorRef) extends Actor {
  import context._
  
  var lastReceive = System.currentTimeMillis()
  var lists = new HashMap[MarketPairRegistration, MutableList[OHLC]]()
  var subscriber = new HashMap[MarketPair, ActorRef]()
  
  def receive() = {
    case t: Transaction => println("DataSource received")
    case r: MarketPair => acceptRegistration(r) // register in fetcher pool, setup mapper,
  }
  
  def acceptRegistration(mp: MarketPair){
    val registration  = new MarketPairRegistration(mp.market, mp.c)
    subscriber += (mp -> sender)
    lists += (registration -> new MutableList[OHLC]())
	pool ! registration
  }
  
  def distribute
  
  
}