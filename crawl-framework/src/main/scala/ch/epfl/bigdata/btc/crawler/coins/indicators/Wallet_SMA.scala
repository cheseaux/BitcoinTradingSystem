package ch.epfl.bigdata.btc.crawler.coins.indicators


class Wallet_SMA (msu : Double, btcsu : Double, maxInv : Double, maxbtc : Double, s :SMA){
  

  var gain =0.0;
  var actualSentiment = 0.0;
  var oldSentiment = 0.0
  var sma =s

  
  val moneySetUp = msu;
  var money =msu;
  
  val btcSetUp = btcsu
  var btcnumber = btcsu;
  
  val maxInvestement = maxInv;
  val maxBtc = maxbtc;
  
  def sentimentUpdate(sentiment : Double){
    oldSentiment = actualSentiment
    actualSentiment = sentiment
    
  }
  def smaUpdate(s: SMA){
    
    sma = s
  }
  def gainUpdate (price : Double) {
    

    
    
   val signal = sma.tradeSignalEnv(0.5) 
   var diff_bt = 0.0
   var diff_money = 0.0
   
   if (signal == 1){
     if(actualSentiment >= oldSentiment ){
       diff_bt = (1+actualSentiment - (1+oldSentiment))*maxBtc
       diff_money = (-1.0)* diff_bt *price 
     }
     else{
       val x = sma.trans(signal, price, maxBtc)
       diff_money = x._1
       diff_bt = x._2 
     }
     while (diff_money > money){
         diff_money = money/2
     }
        
     diff_bt = (-1.0)* diff_money/price
     
   }
   else if(signal == -1){
     if(actualSentiment <= oldSentiment){
       diff_bt = (-1.0)* ((1+actualSentiment) - (1+actualSentiment)) *maxBtc
      
      while (diff_bt > btcnumber){
         diff_bt = btcnumber/2
       }
     
     }
     else{
       val x = sma.trans(signal, price, maxBtc)
       diff_money = x._1
       diff_bt = x._2 
     }
     while (diff_bt > btcnumber){
         diff_bt = btcnumber/2
     }
      diff_money = (-1.0)* diff_bt *price
   }
   
   money = money + diff_money
   btcnumber = btcnumber + diff_bt
   
   gain = (money - moneySetUp) + (btcnumber - btcSetUp)*price
   
   
  }

}