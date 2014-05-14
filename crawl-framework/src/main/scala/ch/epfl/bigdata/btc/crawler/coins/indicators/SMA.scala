package ch.epfl.bigdata.btc.crawler.coins.indicators

import scala.collection.mutable.MutableList

import akka.actor.ActorLogging
import akka.actor.ActorRef
import ch.epfl.bigdata.btc.crawler.coins.types._
import ch.epfl.bigdata.btc.types.Registration._
import com.github.nscala_time.time.Imports._
import ch.epfl.bigdata.btc.types.Transfer._
import ch.epfl.bigdata.btc.types.Indicator._

class SMA(dataSource: ActorRef, watched: MarketPairRegistrationOHLC, period: Int) extends Indicator(dataSource, watched) {

  var observer: MutableList[ActorRef] = new MutableList[ActorRef]()

  var values: List[Double] = Nil
  var time: List[Long] = Nil

  def recompute() {
    values = Nil ::: (movingSum(ticks.map(_.close).toList, period) map (_ / period))

    time = ticks.map(_.date.getMillis()).toList
    observer.map(a => a ! Points(EMA, values zip time))
  }
  def receiveOther(a: Any, ar: ActorRef) {
    a match {
      case actor: ActorRef => observer += actor; println("SMASMASMASMASMASMASMASMASMASMASMASMASMASMAregistered", actor)
      case _ => println("unknown data")
    }
  }

  /* 
	 * the computation of the moving simple moving average was taken from an online forum :
	 * http://stackoverflow.com/questions/1319891/calculating-the-moving-average-of-a-list
	 */
  def simpleMovingAverage(values: List[Double], period: Int): List[Double] = {
    Nil ::: (movingSum(values, period) map (_ / period))
  }

  def movingSum(values: List[Double], period: Int): List[Double] = period match {
    case 0 => throw new IllegalArgumentException
    case 1 => values
    case 2 => values.sliding(2).toList.map(_.sum)
    case odd if odd % 2 == 1 =>
      values zip movingSum(values drop 1, (odd - 1)) map Function.tupled(_ + _)
    case even =>
      val half = even / 2
      val partialResult = movingSum(values, half)
      partialResult zip (partialResult drop half) map Function.tupled(_ + _)
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
  def tradeSignalEnv(values: List[Double], percent: Double): Int = {
    val envA = values.map(_ * (1 + percent))
    val envB = values.map(_ * (1 - percent))

    if (envA.last >= values.last && envA.take(envA.length - 1).last < values.take(values.length - 1).last) {
      1
    } else if (envB.last <= values.last && envB.take(envB.length - 1).last > values.take(values.length - 1).last) {
      -1
    } else
      0
  }
  def trans(ma : List[Double], transactions : List [(Int, Double, Double)], signal : Int, 
	    maxBTC : Double, actualPrice : Double):List[(Int, Double, Double)]={
	  
	  val actual = ma.last
	  var newTrans = transactions
	  val min = ma.min
	  val max = ma.max
	  var price= 0.0
	  var btc = 0.0

	  if(signal == 1 && actual == min){
	    
		 btc = (1- min/max)*maxBTC
	  }
	  else if (signal == -1 && actual == max){
	        
		btc = (1- min/max)*maxBTC 
	  }
	  price = btc * actualPrice
	 (signal, price, btc) :: newTrans
	}
	

}