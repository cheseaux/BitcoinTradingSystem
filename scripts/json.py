#!/usr/bin/python

import sys
import json

with open(sys.argv[1]) as f:
    for line in f:
        try:
            data = json.loads(line)
            text = data["text"]
            date = data["user"]["created_at"]
            author = data["user"]["name"]
            followers = data["user"]["followers_count"]
            lang = data["user"]["lang"]
            if (lang == "en"):
                print "["+str(date)+"]["+author+"]:" + text
            else:
                print "--> ignored language : " + lang
        except KeyError as e:
            print ">>>> ERROR PARSING LINE"
            print e
            
        
		
