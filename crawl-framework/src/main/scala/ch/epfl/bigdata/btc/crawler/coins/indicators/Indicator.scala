package ch.epfl.bigdata.btc.crawler.coins.indicators

import akka.actor.{ ActorSystem, ActorLogging, Actor, Props, ActorRef }
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Registration._
import ch.epfl.bigdata.btc.types.Market._
import ch.epfl.bigdata.btc.crawler.coins.types._
import scala.collection.mutable.MutableList
import org.joda.time.Duration

abstract class Indicator(dataSource: ActorRef, watched: MarketPairRegistrationOHLC) extends Actor {

  var ticks: MutableList[OHLC] = new MutableList[OHLC]()
  dataSource ! watched
  
  println("self", self)

  def receive = {
    case t: OHLC => updateTicks(t); 
    case a: Any => println("Indicator: Any", a); receiveOther(a, sender)
  }

  def updateTicks(tick: OHLC) = {
    //println("ticks.length", ticks.length)
    if (ticks.isEmpty) {
      var currentDate = tick.date.minusMillis(watched.tickSize * 1000 * (watched.tickCount - 1))
      for (a <- 1 to watched.tickCount) {
        ticks += new OHLC(0.0, 0.0, 0.0, 0.0, 0.0, currentDate, new Duration(watched.tickSize))
        currentDate = currentDate.plusMillis(watched.tickSize * 1000)
      }
    }

    val last = ticks.last
    val length = ticks.length

    var idRespectToHead = (tick.date.getMillis() - last.date.getMillis()) / 1000 / watched.tickSize
    var myTicks = ticks
    if (idRespectToHead == 0) { // the same time
      ticks.update(length - 1, tick)
    } else if (idRespectToHead > 0) { // before
      var currentDate = last.date
      for (i <- 1 to idRespectToHead.toInt) {
        currentDate = last.date.plusMillis(watched.tickSize * 1000 * i)
        ticks += new OHLC(0.0, 0.0, 0.0, 0.0, 0.0, currentDate, new Duration(watched.tickSize))
      }
      ticks.update(ticks.length - 1, tick)
      ticks.drop(idRespectToHead.toInt)
    } else if (idRespectToHead < 0 && idRespectToHead > -watched.tickCount) { // insert some in between
      ticks.update(length + idRespectToHead.toInt, tick)
    }
    recompute()
  }

  def recompute()

  def receiveOther(a: Any, ar: ActorRef)
}