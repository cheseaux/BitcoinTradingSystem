package ch.epfl.bigdata.btc.crawler.coins.indicators

import akka.actor.{ ActorSystem, ActorLogging, Actor, Props, ActorRef }

import scala.collection.mutable.MutableList
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.Transfer._


abstract class Wallet[T](dataSource: ActorRef, watched: T, msu : Double, btcsu : Double, 
    maxInv : Double, maxbtc : Double) extends Actor {
  
  private var observer: MutableList[ActorRef] = new MutableList[ActorRef]()
  private var earned = 0.0
  
  
  
  dataSource ! watched
  dataSource ! new TwitterRegistrationFull()
  
  def receive = {
    case actor: ActorRef => observer += actor;
    case t: Tweet => updateTweet(t)
    case a: Any => {
      receiveOther(a, sender)
      earned = gainUpdate();
    }
  }
  
  protected def receiveOther(a: Any, ar: ActorRef) 
  def gainUpdate() : Double
  
  def distribute() {
    observer.map(o => o ! earned)
  }
  
  def updateTweet(t: Tweet)
  
  

}