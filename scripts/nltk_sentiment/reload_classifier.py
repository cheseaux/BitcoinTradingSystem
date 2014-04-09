import pickle

def feature_extractor(doc):
	docwords = set(doc)
	features = {}
	for i in wordlist:
		features['contains(%s)' % i] = (i in docwords)
	return features


f = open('my_classifier.pickle')
classifier = pickle.load(f)
f.close()

f = open('my_classifier_wordlist.pickle')
wordlist = pickle.load(f)
f.close()

text = "Hello, I'm really happy to be here with you"
text2 = "Fuck I hate you scumbag !"

print classifier.classify(feature_extractor(text.split()))
print classifier.classify(feature_extractor(text2.split()))
