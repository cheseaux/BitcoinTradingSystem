import csv, random
import nltk
import tweet_features, tweet_pca
import re
import pickle
import sys
from collections import Counter

class TwitterSentimentClassifier:
	
	def __init__(self,reload=False,classifierPath="classifier.pickle", fListPath="fList.pickle"):
		if reload:
			f = open(fListPath)
			self.featureList = pickle.load(f)
			f.close()
			f = open(classifierPath)
			self.classifier = pickle.load(f)
			f.close()
		else:
			self.featureList = []
			self.classifier = None
	
	@staticmethod
	def getFoldPartition(dataset, kfold):
		for k in xrange(kfold):
			training = [x for i, x in enumerate(dataset) if i % kfold != k]
			validation = [x for i, x in enumerate(dataset) if i % kfold == k]
			yield training, validation
			
	#start getStopWordList
	def getStopWordList(self,filePath):
		#read the stopwords file and build a list
		stopWords = []
	
		fp = open(filePath, 'r')
		line = fp.readline()
		while line:
			word = line.strip()
			stopWords.append(word)
			line = fp.readline()
		fp.close()
		return stopWords

    #start getfeatureVector
	def getFeatureVector(self,tweets, stopWords):
		featureVector = []
		#split tweet into words
		words = tweets.split()
		for w in words:
			#replace two or more with two occurrences
			pattern = re.compile(r"(.)\1{1,}", re.DOTALL)
			w = pattern.sub(r"\1\1", w)
			#strip punctuation
			w = w.strip('\'"?,.')
			#check if the word stats with an alphabet
			val = re.search(r"^[a-zA-Z][a-zA-Z0-9]*$", w)
			#ignore if it is a stop word
			if(w in stopWords or val is None or len(w) <= 3):
				continue
			else:
				featureVector.append(w.lower())
		return featureVector

	
	def clean_tweet(self, tweet):

		# Convert the text to lower case
		post = tweet.lower()
 
		# Remove all urls
		post = re.sub('(http(s?)://)([a-zA-Z0-9\/\.])*', ' ', post)
 
		# Remove everything that is not a word
		post = re.sub('[^(\w&\s)]|\(|\)|\d', ' ', post)
 
		return post
		
	def extract_features2(self,tweet):
		tweet_words = set(tweet)
		features = {}
		for word in self.featureList:
			features['contains(%s)' % word] = (word in tweet_words)
			if word in tweet_words:
				print "has : " + word
		return features
		
	def extract_features(self,tweet):
		tweet_words = set(tweet)
		features = {}
		for word in self.featureList:
			features['contains(%s)' % word] = (word in tweet_words)
		return features
		
	def classifyTweet(self, tweet):
		processedTweet = self.clean_tweet(tweet)
		stopWords = self.getStopWordList('stopwords.txt')
		featureVec = self.getFeatureVector(processedTweet, stopWords)
		#print self.extract_features(featureVec)
		return self.classifier.classify(self.extract_features2(featureVec))
		
	def train(self, filePath, classificationModel, crossValidation=False):
		
		#Read the tweets one by one and process it
		inpTweets = csv.reader(open(filePath, 'rb'), delimiter=',', quotechar='"')
		stopWords = self.getStopWordList('stopwords.txt')
		
		# Get tweet words
		balanceSentiment = {'positive':0,'negative':0,'neutral':0}
		
		tweets = []
		for row in inpTweets:
			sentiment = row[1]
			if sentiment != 'positive' and sentiment != 'negative' and sentiment != 'neutral':
				continue
			#Want equal proportion of neg/pos/neutral tweets
			#Since we have only 691 neg tweets, it is the upperband
			if balanceSentiment[sentiment] >= 691:
				continue
			balanceSentiment[sentiment] += 1
			tweet = row[0]
			processedTweet = self.clean_tweet(tweet)
			featureVector = self.getFeatureVector(processedTweet, stopWords)
			self.featureList.extend(featureVector)
			tweets.append((featureVector, sentiment));
		#end loop
		#print balanceSentiment
		
		wordOccurences = Counter(self.featureList).most_common(1000)
		self.featureList = zip(*wordOccurences)[0]
		
		# Extract feature vector for all tweets in one shot
		fvecs = nltk.classify.util.apply_features(self.extract_features, tweets)

		if not crossValidation:
			self.classifier = classificationModel.train(fvecs);
			#self.classifier.show_most_informative_features(10)
			f = open('classifier.pickle', 'wb')
			pickle.dump(self.classifier, f)
			f.close()
		
			f = open('fList.pickle', 'wb')
			pickle.dump(self.featureList, f)
			f.close()
		else:
			
			kfold = 5
			current = 0
			print '########################'
			print '%d-fold crossvalidation' % kfold
			print '########################'
			print 'Iteration\tAccuracy'
			for (v_train, v_test) in self.getFoldPartition(fvecs, kfold):
				current += 1
				tweetCount = len(tweets)
				test_size = int(tweetCount/kfold)
				train_size = tweetCount-test_size

				# train classifier
				self.classifier = classificationModel.train(v_train);
			
				# classify and dump results for interpretation
				#self.classifier.show_most_informative_features(10)
				accuracy = nltk.classify.accuracy(self.classifier, v_test)
				print '%d\t\t%f' % (current, accuracy)

if __name__=='__main__':
	classificationModel = nltk.NaiveBayesClassifier
	sentimentClassifier = TwitterSentimentClassifier(reload=False)
	sentimentClassifier.train('mergedWithApple.csv', classificationModel, crossValidation=True)

## build confusion matrix over test set
#test_truth   = [s for (t,s) in v_test]
#test_predict = [classifier.classify(t) for (t,s) in v_test]

#print 'Confusion Matrix'
#print nltk.ConfusionMatrix( test_truth, test_predict )
