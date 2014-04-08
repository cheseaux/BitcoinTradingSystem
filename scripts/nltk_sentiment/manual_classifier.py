import nltk
from nltk.corpus import stopwords
 
#Adapted from :
#http://www.sjwhitworth.com/sentiment-analysis-in-python-using-nltk/
 
#Load positive tweets into a list
pos = open('corpus/movie_reviews/pos_movies.txt', 'r')
postxt = pos.readlines()
 
#Load negative tweets into a list
neg = open('corpus/movie_reviews/neg_movies.txt', 'r')
negtxt = neg.readlines()

neglist = []
poslist = []
 
#Create a list of 'negatives' with the exact length of our negative tweet list.
for i in range(0,len(negtxt)):
	neglist.append('negative')
 
#Likewise for positive.
for i in range(0,len(postxt)):
	poslist.append('positive')
 
#Creates a list of tuples, with sentiment tagged.
postagged = zip(postxt, poslist)
negtagged = zip(negtxt, neglist)
 
#Combines all of the tagged tweets to one large list.
taggedtweets = postagged + negtagged

tweets = []
 
#Create a list of words in the tweet, within a tuple.
for (word, sentiment) in taggedtweets:
	word_filter = [i.lower() for i in word.split()]
	tweets.append((word_filter, sentiment))
 
#Pull out all of the words in a list of tagged tweets, formatted in tuples.
def getwords(tweets):
	allwords = []
	for (words, sentiment) in tweets:
		allwords.extend(words)
	return allwords
 
#Order a list of tweets by their frequency.
def getwordfeatures(listoftweets):
	#Print out wordfreq if you want to have a look at the individual counts of words.
	wordfreq = nltk.FreqDist(listoftweets)
	words = wordfreq.keys()
	return words
 
#Calls above functions - gives us list of the words in the tweets, ordered by freq.
wordlist = getwordfeatures(getwords(tweets))

def feature_extractor(doc):
	docwords = set(doc)
	features = {}
	for i in wordlist:
		features['contains(%s)' % i] = (i in docwords)
	return features
 
#Creates a training set - classifier learns distribution of true/falses in the input.
wordlist = [i for i in wordlist if not i in stopwords.words('english') and not i in extra_stop_words]
training_set = nltk.classify.apply_features(feature_extractor, tweets)
classifier = nltk.NaiveBayesClassifier.train(training_set)
print classifier.show_most_informative_features(n=200)

text = "I hate you fucking bitch"
print "Sentiment of : " + text + " is \n\tt" + classifier.classify(feature_extractor(text))

