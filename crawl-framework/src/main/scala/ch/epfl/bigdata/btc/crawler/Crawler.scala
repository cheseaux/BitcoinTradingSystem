package ch.epfl.bigdata.btc.crawler

trait Crawler {
	def doCrawl(): Unit
	def doUpdate(): Unit
	def notifyListener(): Unit
}