package ch.epfl.bigdata.btc.crawler.btc

import org.apache.http.client.fluent._;

object RequestFactory {
	var mock: Request = _
	
	def Get(uri: String) : Request = mock match {
	  case _ : Request => mock
	  case _ => Request.Get(uri)
	}
	
	def Post(uri: String) : Request = mock match {
	  case _ : Request => mock
	  case _ => Request.Post(uri)
	}
	
	def setInstance(inst: Request) = mock = inst
	
}