
#import requests
#from bs4 import BeautifulSoup
##http://www.cryptocoinsnews.com/?p=13646
#URL = "http://www.cryptocoinsnews.com/?p="

##for i in range(5000,50000) :
#r = requests.get(URL + str(13646))
#if (r.status_code == 200) :
	#print "200 OK"
	##to find : <section class="entry-content clearfix"
	#soup = BeautifulSoup(r.text)
	##soup.find("section", {"class": "entry-content clearfix"})
	#section = soup.findAll("section", {"itemprop":"articleBody"})
	
import urllib2
from bs4 import BeautifulSoup

for i in range(2000,50000) :

	if (i % 100 == 0):
		print "Scanning progress ("+str(i) + "/" + "50000) : " + str(round(i /(50000.0 - 200.0)*100.0)) + "%"

	site= "http://www.cryptocoinsnews.com/?p=" + str(i)
	hdr = {'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11',
       'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
       'Accept-Charset': 'ISO-8859-1,utf-8;q=0.7,*;q=0.3',
       'Accept-Encoding': 'none',
       'Accept-Language': 'en-US,en;q=0.8',
       'Connection': 'keep-alive'}

	req = urllib2.Request(site, headers=hdr)
	
	try:
		page = urllib2.urlopen(req)
	except urllib2.URLError, e:
		pass
	else:
		# 200
		soup = BeautifulSoup(page)

		headline = soup.find('h1', {'itemprop':'headline'})
		time =  soup.find('time', {'class':'updated'})
		if (headline != None and time != None):
			print "[" + str(i) + "]["+str(time.getText())+"] " + str(headline.getText()) + "\r\n"
