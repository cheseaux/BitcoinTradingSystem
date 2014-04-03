package ch.epfl.bigdata.btc.crawler.coins

import ch.epfl.bigdata.btc.crawler.coins.indicators.Indicator
import ch.epfl.bigdata.btc.crawler.coins.types.Transaction
import scala.collection.mutable.MutableList

import akka.actor.{Actor, ActorRef, Props}


class DataSource extends Actor {
  import context._
  
  var lastReceive = System.currentTimeMillis()
  
  def receive() = {
    case t: Transaction => if (System.currentTimeMillis()-lastReceive > 10000) {
      println("DataSource received")
      lastReceive = System.currentTimeMillis()
    }
  }
}