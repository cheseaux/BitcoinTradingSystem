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

class EMA(dataSource: ActorRef, watched: MarketPairRegistrationOHLC, period: Int, alpha: Double) extends Indicator[Points](dataSource, watched, 10000) {

  var values: List[Double] = Nil
  var time: List[Long] = Nil
  var oldEMA: List[(Double, Long)] = Nil
  var first = true

  def recompute() {
    //values = Nil ::: (exponentialMovingAverage(ticks.map(_.close).toList, period, alpha))
    //time = ticks.map(_.date.getMillis()).toList
    println(ticks);
   movingSumExponential(ticks.map(_.close).toList, ticks.map(_.date.getMillis()).toList, period, alpha)
  }

  def getResult() = Points(EMA, oldEMA.reverse)

  /*def exponentialMovingAverage(values: List[Double], period: Int, alpha: Double): List[Double] = {
    Nil ::: (movingSumExponential(values, period, alpha))
  }
  * */
  
  def movingSumExponential(newValues: List[Double], newTime: List[Long],  period: Int, alpha1: Double){

    var old =0.0
    var newv=0.0
    val alpha = 1.0 / (2.0 * period.toDouble + 1.0)
    var newt : Long =0
    if (first) {
      for(i <- 0 to newValues.length - 2){
        newv = newValues.drop(i).head
        newt = newTime.drop(i).head
        if( newv != 0){
         
          oldEMA ::= (newv * alpha + old*(1-alpha), newt)
          old = newv
        }
 
      }
        
      oldEMA ::= (newValues.last, newTime.last)
      first = false
    }

    else {
    	
    	var toAdd = newValues.last * alpha + (1.0 - alpha) * oldEMA.head._1
    
    	oldEMA ::= (toAdd, newTime.last)
    }
    if (oldEMA.length > period)
      oldEMA = oldEMA.take(period)
      /*
    for (i <- 0 to values.length - period - 1) {
      if (oldEMA.length -1 > i) {
        finalList ::= oldEMA.drop(i).head
      }

    }
    values = finalList.reverse
    * 
    */
  }

  /*def movingSumExponential(values: List[Double], period: Int, alpha: Double): List[Double] = period match {
    case 0 => throw new IllegalArgumentException
    case 1 => values
   

    case _ =>

      
      val listCoeff = exponentialList(period, alpha)
      var finalList: List[Double] = Nil
      for (i <- 0 to values.length - period)
        finalList ::= (values.drop(i).take(period), listCoeff).zipped.map(_ * _).sum / listCoeff.sum

      finalList.reverse

  }
  def exponentialList(period: Int, alpha: Double): List[Double] =  {
    

      var list: List[Double] =  Nil
      list::= 1.0
      val element = 1 - alpha
      var toAdd = 1.0
      for (i <- 0 to (period - 2)) {
        toAdd = toAdd * element
        list ::= toAdd
      }
      
      list
  }
*/
  def receiveOther(a: Any, ar: ActorRef) {
    a match {
      case _ => println("Class:EMA, received unknown data")
    }
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
  def tradeSignalEnv( percent: Double): Int = {
    val envA = values.map(_ * (1 + percent))
    val envB = values.map(_ * (1 - percent))

    if (envA.last >= values.last && envA.take(envA.length - 1).last < values.take(values.length - 1).last) {
      1
    } else if (envB.last <= values.last && envB.take(envB.length - 1).last > values.take(values.length - 1).last) {
      -1
    } else
      0
  }

  def trans(signal: Int, actualPrice: Double, maxBTC : Double): (Double, Double) = {

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