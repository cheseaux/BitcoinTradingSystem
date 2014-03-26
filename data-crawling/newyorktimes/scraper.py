import json
import urllib2

from datetime import date, timedelta

apiUrl='http://api.nytimes.com/svc/search/v1/article?format=json'
query='query=bitcoin'                           
apiDate='begin_date=20110901&end_date=20120214' 
fields='fields=body%2Curl%2Ctitle%2Cdate%2Cdes_facet%2Cdesk_facet%2Cbyline'
offset='offset=0'
key='api-key=397ab939332fd29f69dab0e83e66987c:4:69065069'  # input you

def remove_html_tags(data):
    p = re.compile(r'<.*?>')
    return p.sub('', data)
    
def increment_date(from_date, days):
	return from_date + timedelta(days)


def crawl_news(topic, start_date, end_date, results_number=20):
	result_count = 0
	
	apiDate='from-date='+str(from_date)+'&to-date='+str(to_date)  
	link=[apiUrl, apiDate, apiPage, section, apiPageSize, fields, key]
	ReqUrl='&'.join(link)
	jstr = urllib2.urlopen(ReqUrl).read()
	ts = json.loads(jstr)
	result_count += len(ts['response']['results'])
	if result_count > 0 :
		print "Crawling from " + str(from_date) + " to " + str(to_date) + "\r\n"
		for result in ts['response']['results']:
			date = result['fields']['newspaperEditionDate'] 
			headline = result['fields']['headline']
			shortUrl = result['fields']['shortUrl']
			print "[" + date + "] " + headline + "\r\n URL : " + shortUrl
		#print result['fields']['trailText'] + "\r\n"
	
	return result_count
	
from_date = date(2009,1,1)
fetch_interval_days = 1
to_date = increment_date(from_date, fetch_interval_days)

results_count = 0

while to_date < date.today() : 
	results_count += crawl_news('Bitcoin', from_date, to_date)
	from_date = increment_date(from_date, fetch_interval_days)
	to_date = increment_date(from_date, fetch_interval_days)
	if (to_date > date.today()) : to_date = date.today()
	
print "Total results : " + str(results_count)
