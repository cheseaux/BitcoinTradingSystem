package ch.epfl.bigdata.btc.crawler.coins.indicators

import akka.actor.{ ActorSystem, ActorLogging, Actor, Props, ActorRef }
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.Market._
import ch.epfl.bigdata.btc.crawler.coins.types._
import scala.collection.mutable.MutableList
import org.joda.time.Duration
import org.joda.time.DateTime

/**
 * @param dataSource a reference to the data source / master actor
 * @param watched the market and currency pair the indicator should work on
 * @param sensibility the minimal wait time between executions
 *
 * @param T the return type of the getResult method, such that observer notification can be automated
 */
abstract class Indicator[T](dataSource: ActorRef, watched: MarketPairRegistrationOHLC, sensibility: Long) extends Actor {

  protected var ticks: MutableList[OHLC] = new MutableList[OHLC]()
  private var lastUpdate: DateTime = DateTime.now().minusMillis(10000)
  private var observer: MutableList[ActorRef] = new MutableList[ActorRef]()
  dataSource ! watched

  /**
   * executes the algorithm on the available data
   */
  protected def recompute()

  /**
   * Should return the result of the computations
   */
  protected def getResult(): T

  /**
   * Delegation of message handling for unknown type
   */
  protected def receiveOther(a: Any, ar: ActorRef)
  
  def receive = {
    case t: OHLC => doUpdateDataAndDistribute(t)
    case actor: ActorRef => observer += actor;
    case a: Any => receiveOther(a, sender)
  }

  /**
   * Updates the local OHLC cache and reruns the algorithm if
   * it wasn't executed for some time, the distributes the result to the observers
   */
  def doUpdateDataAndDistribute(t: OHLC) {
    updateTicks(t)
    
    if (lastUpdate.getMillis() - DateTime.now().getMillis() < -sensibility) {
      recompute()
      var r: T = getResult()
      observer.map(a => a ! r)
      lastUpdate = DateTime.now()
    }
  }

  /**
   * Updates the local OHLC cache with the received OHLC
   */
  def updateTicks(tick: OHLC) = {
    if (ticks.isEmpty) {
      var currentDate = tick.date.minusMillis(watched.tickSize * 1000 * (watched.tickCount - 1))
      for (a <- 1 to watched.tickCount) {
        ticks += new OHLC(0.0, 0.0, 0.0, 0.0, 0.0, currentDate, new Duration(watched.tickSize))
        currentDate = currentDate.plusMillis(watched.tickSize * 1000)
      }
    }
    val last = ticks.last // most recent
    val length = ticks.length
    var idRespectToHead = (tick.date.getMillis() - last.date.getMillis()) / 1000 / watched.tickSize
    var myTicks = ticks
    if (idRespectToHead == 0) { // the same time
      ticks.update(length - 1, tick)
    } else if (idRespectToHead > 0) { // before
      var currentDate = last.date
      for (i <- 1 to idRespectToHead.toInt) {
        currentDate = last.date.plusMillis(watched.tickSize * 1000 * i)
        ticks += new OHLC(last.close, last.close, last.close, last.close, 0.0, currentDate, new Duration(watched.tickSize))
      }
      ticks.update(ticks.length - 1, tick)
      ticks.drop(idRespectToHead.toInt)
    } else if (idRespectToHead < 0 && idRespectToHead > -watched.tickCount) { // insert some in between
      ticks.update(length + idRespectToHead.toInt, tick)
    }
  }
}
