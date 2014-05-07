#!/usr/bin/python

import json
from datetime import datetime
from sentiment import *
from collections import OrderedDict

class SentimentCorrelation():
	
	def __init__(self):
		self.sentiment_per_day = {}
		self.classifier = TwitterSentimentClassifier(load=True)
		self.sentiment_to_int = {'positive':1,'negative':-1,'neutral':0}
		self.prices = []
		self.sentiments = []
		
	def load_prices(self, path):
		#Timestamp	Open	High	Low	   Close
		with open(path) as tsv:
			for line in csv.reader(tsv, dialect="excel-tab"):
				self.prices.append(float(line[4]))
		
	def load_tweets(self, path):
		with open(path) as f:
		    for line in f:
		        data = json.loads(line)
		        lang = data["user"]["lang"]
		        if lang == "en" :
					text = data['text']
					sentiment = self.classifier.classify_tweet(text)
					date = datetime.strptime(data["created_at"],'%a %b %d %H:%M:%S +0000 %Y').date().isoformat();
					if not date in self.sentiment_per_day:
						self.sentiment_per_day[date] = self.sentiment_to_int[sentiment]
					else:
						self.sentiment_per_day[date] += self.sentiment_to_int[sentiment]
		ordered = OrderedDict(sorted(corr.sentiment_per_day.items(), key=lambda t: t[0]))
		self.sentiments = ordered.values()

if __name__=='__main__':
	corr = SentimentCorrelation()
	corr.load_tweets('bitcoin_tweets_december_2013.json')
	corr.load_prices('december_prices.txt')
	print corr.prices
	print corr.sentiments
	
