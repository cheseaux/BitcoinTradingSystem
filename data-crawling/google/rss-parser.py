import feedparser

RSS_URL = 'http://www.google.com/alerts/feeds/04790032051601382320/7371668903503113350'

rss_feed = feedparser.parse(RSS_URL)

for key, value in rss_feed.items(): print key
