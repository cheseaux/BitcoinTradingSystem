package ch.epfl.bigdata.btc.crawler.coins.indicators

import akka.actor.{ ActorSystem, ActorLogging, Actor, Props, ActorRef }

import scala.collection.mutable.MutableList
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.Transfer._


abstract class Wallet[T](dataSource: ActorRef, watched: T) extends Actor {
  
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
  
  def receive = {
    case actor: ActorRef => observer += actor;
    case t: Tweet => updateTweet(t)
    case tr : Transaction => updatePrice(tr)
    case a: Any => {
      receiveOther(a, sender)
      earned = gainUpdate();
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