package ch.epfl.bigdata.btc.crawler

object Running_class {


    def main(args: Array[String]) {
   
      var  list : List[Double] = Nil
      for(i <- 1 to 24){
    	  	list::=i.toDouble
    	  	
      }
     val testSMA =  simpleMovingAverage(list, 12)
     list =list.reverse
     println(list)
    val  period = 24
      println( Math.ceil(period.toDouble).toInt)
    
      val listCoeff = exponentialList(12, 0.5);
      println(listCoeff)
      
      println((list.drop(1).take(12), listCoeff).zipped.map(_ * _).sum / listCoeff.sum)
      println(24/2)
      
     
      val listExp = movingSumExponential(list, 24, 0.5)
      println(listExp)
      
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
	
	def simpleMovingAverage(values: List[Double], period: Int): List[Double] = {
		Nil ::: (movingSum(values, period) map (_ / period))
	}
	def exponentialMovingAverage(values: List[Double], period: Int, alpha : Double): List[Double] ={
	  Nil ::: (movingSumExponential(values, period, alpha))
	}
	def movingSumExponential(values: List[Double], period: Int, alpha : Double): List[Double] = period match {
		case 0 => throw new IllegalArgumentException
		case 1 => values
		case odd if odd % 2 == 1 => 
		  var halfPeriod = Math.ceil(period.toDouble/2).toInt
		  val listCoeff = exponentialList(halfPeriod, alpha)
		  var finalList : List[Double] = Nil
		  for(i <- 0 to halfPeriod  )
		   finalList ::= (values.drop(i).take(halfPeriod), listCoeff).zipped.map(_ * _).sum / listCoeff.sum		    
		  
		finalList.reverse 
		case even => 
		  var halfPeriod = period/2
		  val listCoeff = exponentialList(halfPeriod, alpha)
		  var finalList : List[Double] = Nil
		  for(i <- 0 to halfPeriod )
		   finalList ::= (values.drop(i).take(halfPeriod), listCoeff).zipped.map(_ * _).sum / listCoeff.sum		    
		  
		finalList.reverse 
		
	}
	def exponentialList (period : Int, alpha : Double ): List[Double] = period match{
	  case 0 => throw new IllegalArgumentException
	  case 1 => List(1)
	  case _  => 
	    var list : List[Double]= Nil
	    val element = 1 - alpha
	    var toAdd =0.0
	    for(i <- 0 to (period -1 )){
	      toAdd = Math.pow(element, i)
	      list::= toAdd
	    }
	   list  
	}
	
	
}