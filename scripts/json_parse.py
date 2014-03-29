#!/bin/sh

dir=$1
keyword="bitcoin"

echo "Unzipping zip archive..."
unzip -d unzipped/ $1 >/dev/null 2>&1
echo "Done!"
echo "Unzipping individual json files..."
bzip2 -d unzipped/*/*/*.bz2 >/dev/null 2>&1
echo "Done!"
echo "Filtering tweets with keyword $keyword..."
cat unzipped/*/*/*.json | grep $keyword > aggregate.json
echo "Done!"
echo "Deleting temporary files..."
rm -rf unzipped
echo "Done!"

# Relevant keys/values pairs in json :

#{"retweet_count":0,
#"text":"RT @all2sgaro: Buy Bitcoin in Asia with new Bitcoin exchange called Ruxum http:\/\/t.co\/hBf9Bl6A: RT @all... http:\/\/t.co\/mdkrDLSk #bitcoin",
#"retweeted":false
#"created_at":"Sat Oct 15 03:40:57 +0000 2011",


