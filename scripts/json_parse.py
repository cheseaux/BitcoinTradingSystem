#!/usr/bin/python

import sys
import json
from datetime import datetime
import subprocess

with open(sys.argv[1]) as f:
    maxFollowers = 0;
    maxFollowersName = "";
    maxRetweets = 0;
    maxRetweetsName = "";
    for line in f:
        try:
            data = json.loads(line)
            text = data["text"]
	    #proc = subprocess.Popen("java -cp \"*\" -mx5g edu.stanford.nlp.sentiment.SentimentPipeline -stdin", stdout=subprocess.PIPE,shell=True)
            #(out, err) = proc.communicate()
	    #print "Sentiment : " + out
	    retweets = data["retweet_count"]
	    date = datetime.strptime(data["created_at"],'%a %b %d %H:%M:%S +0000 %Y');
            author = data["user"]["name"]
            followers = data["user"]["followers_count"]
            lang = data["user"]["lang"]
            if lang == "en" and "bitcoin" in text and followers > 100 and not text.startswith("RT") and not text.startswith("@"):
		if (followers > maxFollowers):
	            maxFollowers = followers;
		    maxFollowersName = author;	    
		if (retweets > maxRetweets):
		    maxRetweets = retweets;
		    maxRetweetsName = author;
		print text.encode('utf-8')
        except KeyError as e:
            print ">>>> ERROR PARSING LINE"
            print e
#    print str(maxRetweetsName) + " has been retweeted " + str(maxRetweets) + " times!"
#    print str(maxFollowersName) + "has " + str(maxFollowers) + " followers"
     
          
		
