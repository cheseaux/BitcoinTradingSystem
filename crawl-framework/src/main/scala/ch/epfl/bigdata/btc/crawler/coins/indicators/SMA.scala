package ch.epfl.bigdata.btc.crawler.coins.indicators

import scala.collection.mutable.MutableList
import akka.actor.ActorLogging
import akka.actor.ActorRef
import ch.epfl.bigdata.btc.crawler.coins.types._
import ch.epfl.bigdata.btc.types.Registration._
import com.github.nscala_time.time.Imports._
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Indicator._
import org.joda.time.DateTime

class SMA(dataSource: ActorRef, watched: MarketPairRegistrationOHLC, period: Int) extends Indicator[Points](dataSource, watched, 10000) {

  var values: List[Double] = Nil
  var time: List[Long] = Nil

  def recompute() {
    //println("SMA-recompute", DateTime.now())
    //println("Ticks ", ticks.map(_.close).toList zip ticks.map(_.date.getMillis()).toList)
    values = Nil ::: (movingSum(ticks.map(_.close).toList, period))

    time = ticks.iterator.filter(_.close > 0.0).toList.map(_.date.getMillis()).toList
  }

  def getResult() = Points(SMA, values zip time)

  def receiveOther(a: Any, ar: ActorRef) {
    a match {
      case _ => println("Class:SMA, received unknown data")
    }
  }

  /* 
	 * the computation of the moving simple moving average was taken from an online forum :
	 * http://stackoverflow.com/questions/1319891/calculating-the-moving-average-of-a-list
	 */
  def simpleMovingAverage(values: List[Double], period: Int): List[Double] = {
    Nil ::: (movingSum(values, period))
  }
  def movingSum(values: List[Double], period: Int): List[Double] = period match {
    case 0 => throw new IllegalArgumentException
    case 1 => values
    case _ =>
      var finalList: List[Double] = Nil
      for (i <- 0 to values.length - period) {
        finalList ::= (values.drop(i).take(period).iterator.filter(_ > 0.0).toList).sum / (values.drop(i).take(period).iterator.filter(_ > 0.0).toList).length
      }
      finalList.reverse
  }

  def envellopAbove(values: List[Double], percent: Double): List[Double] = {
    values.map(_ * (1 + percent))

  }
  def envellopBelow(values: List[Double], percent: Double): List[Double] = {
    values.map(_ * (1 - percent))

  }
  /*
  *  ind represents the list you want as indicator (i.e. the long-term MA)
  *  The value -1 indicates that the price should go down and
  *  a value of 1 that the price will go up.
  */

  def tradeSignalCO(values: List[Double], ind: List[Double]): Int = {

    if (ind.last >= values.last && ind.take(ind.length - 1).last < values.take(values.length - 1).last) {
      -1
    } else if (ind.last <= values.last && ind.take(ind.length - 1).last > values.take(values.length - 1).last) {
      1
    } else
      0
  }

  /*
	 * For this function the percent is the size of the envellope you want to have,
	 * In this case, a value of 1 would state that the price will go up, as
	 * a value of -1 indicates that the price will go down.	 * 
	 */
  def tradeSignalEnv(percent: Double): Int = {
    val envA = values.map(_ * (1 + percent))
    val envB = values.map(_ * (1 - percent))

    if (envA.last >= values.last && envA.take(envA.length - 1).last < values.take(values.length - 1).last) {
      1
    } else if (envB.last <= values.last && envB.take(envB.length - 1).last > values.take(values.length - 1).last) {
      -1
    } else
      0
  }
  def trans(values : List [Double], signal: Int, actualPrice: Double, maxBTC : Double): (Double, Double) = {

    val actual = values.last

    val min = values.min
    val max = values.max
    
    var btc=0.0
    var money = 0.0

    if (signal == 1 && actual == min) {

      btc = (1 - (min) / (max)) * maxBTC
      money = (-1)* actualPrice* btc
    } else if (signal == -1 && actual == max) {

      btc = (-1)*(1 - (min) / (max)) * maxBTC
      money = (-1)*actualPrice*btc
    }
    (money, btc)
  }

}