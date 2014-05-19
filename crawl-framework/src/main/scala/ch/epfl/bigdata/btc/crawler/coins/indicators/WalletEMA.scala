package ch.epfl.bigdata.btc.crawler.coins.indicators

import akka.actor.ActorRef
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.Transfer._

class WalletEMA(dataSource: ActorRef, watched: EMARegistration, msu : Double, btcsu : Double, 
    maxInv : Double, maxbtc : Double)
    extends Wallet[EMARegistration] (dataSource, watched, msu, btcsu, maxInv, maxbtc) {

  protected def receiveOther(a: Any, ar: ActorRef) {
    //case a : Points => doSmth()
  }
  
  def gainUpdate() : Double = {
    return 0.0
  }
  
  def updateTweet(t: Tweet) {
    
  }
  
  
}