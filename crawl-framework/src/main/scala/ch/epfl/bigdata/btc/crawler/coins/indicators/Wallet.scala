package ch.epfl.bigdata.btc.crawler.coins.indicators


class Wallet (msu : Double, btcsu : Double, maxInv : Double, maxbtc : Double){
  
  var wallet :List[(Double, Int, Double)] = Nil
  var gain =0.0;
  var actualSentiment = 0.0;
  
  val moneySetUp = msu;
  var money =msu;
  
  val btcSetUp = btcsu
  var btcnumber = btcsu;
  
  val maxInvestement = maxInv;
  val maxBtc = maxbtc;
  
  def walletUpdate(price : Double, signal :Int, sentiment : Double){
    
    wallet ::= (price, signal, sentiment)
    
  }
  def sentimentUpdate(sentiment : Double){
    actualSentiment = sentiment
  }
  
  def gainUpdate () {
    
   val signal = wallet.head._2
   val price = wallet.head._1
   val sentiment = wallet.head._3
   var diff_bt = 0.0
   var diff_money = 0.0
   
   if (signal == 1){
     if(sentiment >= actualSentiment ){
       diff_bt = (1+sentiment - (1+actualSentiment))*maxBtc
       diff_money = (-1.0)* diff_bt *price
       if (diff_money > money){
         diff_money = money/2
       }
        diff_bt = (-1.0)* diff_money/price
     }
     
   }
   else if(signal == -1){
     if(sentiment <= actualSentiment){
       diff_bt = (-1.0)* (1+actualSentiment - (1+sentiment)) *maxBtc
       diff_money = (-1.0)* diff_bt *price
      if (diff_bt > btcnumber){
         diff_bt = btcnumber/2
       }
     
     }
     
   }
   
   money = money + diff_money
   btcnumber = btcnumber + diff_bt
   
   gain = (money - moneySetUp) + (btcnumber - btcSetUp)*price
   
   
  }

}