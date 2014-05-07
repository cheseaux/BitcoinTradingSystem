package ch.epfl.bigdata.btc.crawler.coins.indicators

import scala.collection.mutable.MutableList

import akka.actor.ActorLogging
import akka.actor.ActorRef
import ch.epfl.bigdata.btc.crawler.coins.types._
import ch.epfl.bigdata.btc.types.Registration._

class SMA(dataSource: ActorRef, watched: MarketPairRegistrationOHLC, period: Int) extends Indicator(dataSource, watched) {

	var values: List[Double] = Nil

	def recompute() {		
		values = Nil ::: (movingSum(ticks.map(_.close).toList, period) map (_ / period))
	}
	
	
	/* 
	 * the computation of the moving simple moving average was taken from an online forum :
	 * http://stackoverflow.com/questions/1319891/calculating-the-moving-average-of-a-list
	 */
	def simpleMovingAverage(values: List[Double], period: Int): List[Double] ={
		Nil ::: (movingSum(values, period) map (_ / period))
	}

	def movingSum(values: List[Double], period: Int): List[Double] = period match {
		case 0 => throw new IllegalArgumentException
		case 1 => values
		case 2 => values.sliding(2).toList.map(_.sum)
		case odd if odd % 2 == 1 => 
			values zip movingSum(values drop 1, (odd - 1)) map Function.tupled(_+_)
		case even =>
			val half = even / 2
			val partialResult = movingSum(values, half)
			partialResult zip (partialResult drop half) map Function.tupled(_+_)
	}
	
	
	
}