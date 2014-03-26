package ch.epfl.bigdata.btc.crawler.btc

import org.apache.http.client.fluent.Request

class HCFluentI {
  
  
  private var request : Request = Request.Get("");
  private var useFake : Boolean = false;
  
  def Get(uri: String) : Request = {
    if (this.useFake) {
      return request
    } else {
      return Request.Get(uri)
    }
  }
  
  def Post(uri: String) : Request = {
    if (this.useFake) {
      return request
    } else {
      return Request.Post(uri)
    }
  }
  
  def setMock(req : Request) = {
    this.request = req
  }

}