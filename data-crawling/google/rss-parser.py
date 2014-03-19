from datetime import date, datetime, timedelta
from bs4 import BeautifulSoup
import requests


# Search settings
START_DATE = date(2014,1,14)
SEARCH_INTERVAL = 30
END_DATE = START_DATE + timedelta(days=SEARCH_INTERVAL)



def generateSearchURL(topic, start_date, end_date, offset=0, lang='en', location='us'):
	return 0


def increment_date(days=SEARCH_INTERVAL):
	global START_DATE
	global END_DATE
	START_DATE += timedelta(days)
	END_DATE = START_DATE + timedelta(days=SEARCH_INTERVAL)

def dateToGoogleFormat(date):
	return "%d/%d/%d"%(date.month, date.day, date.year)

#Google news classes :
	
	#l _xc
	
	

URL = "https://www.google.com/search?q=bitcoin&authuser=0&hl=en&gl=us&noj=1&tbs=cdr:1,cd_min:3/4/2014,cd_max:3/5/2014,sbd:1&source=lnms&tbm=nws&sa=X"

def listArticles(url):
	print "Fetching url : " + url
	r = requests.get(url, headers=headers)
	data = r.text
	soup = BeautifulSoup(data)
	soup.find("a", {"class": "l"})
	liTags = soup.findAll("li", {"class":"g"})

	for article in liTags:
		print article.find('a').contents

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6',
	'accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
	'accept-encoding':'gzip,deflate,sdch',
	'accept-language':'en-US;q=0.8,en;q=0.6',
	'cache-control':'max-age=0',
	'user-agent':'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36',
}


for x in range(0,1):
	#print 'Start date : ' + str(START_DATE) + " , end date : " + str(END_DATE)
	#url =  generateSearchURL('bitcoin', dateToGoogleFormat(START_DATE),dateToGoogleFormat(END_DATE))
	listArticles("https://www.google.ch/search?q=bitcoin&&hl=en&tbs=cdr:1,cd_min:3/4/2014,cd_max:3/4/2014,sbd:1&tbm=nws")
	#increment_date()



