package ch.epfl.bigdata.btc.crawler.coins.indicators

import akka.actor.{ ActorSystem, ActorLogging, Actor, Props, ActorRef }

import scala.collection.mutable.MutableList
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.CurrencyPair
import ch.epfl.bigdata.btc.types.Market._


abstract class Wallet[T](dataSource: ActorRef, watched: T, m: Market, c: CurrencyPair) extends Actor {
  
  private var observer: MutableList[ActorRef] = new MutableList[ActorRef]()
  private var earned = 0.0
  var actualSentiment = 0.0
  var oldSentiment = 0.0
  var actualPrice = 0.0
  var oldPrice = 0.0
  val msu = 100000.0
  var money = msu
  val btcsu = 100.0
  var btcnumber = btcsu
  val maxbtc = 0.1

  
  dataSource ! watched
  dataSource ! new TwitterRegistrationFull()
  dataSource ! new MarketPairRegistrationTransaction(m, c)
  
  def receive = {
    case actor: ActorRef => observer += actor;
    case t: Tweet => {
      updateTweet(t)
      earned = gainUpdate()
      distribute()
    }
    case tr : Transaction => {
      updatePrice(tr)
      earned = gainUpdate()
      distribute()
    }
    case a: Any => {
      receiveOther(a, sender)
      
    }
  }
  
  
  /**
   * Delegation of message handling for unknown type
   */
  protected def receiveOther(a: Any, ar: ActorRef)
  

  
  
  def gainUpdate() : Double
  
  def distribute() {
    observer.map(o => o ! earned)
  }
  
  def updateTweet(t: Tweet)
  def updatePrice(t : Transaction)
  

}