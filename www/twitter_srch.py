from twython import Twython
from async_client import AsyncClient
import re, string, sys

APP_KEY = 'n5LFANrACgG4jPvT1xxCDsJ6a'
APP_SECRET = 'rZmQ7b1xSguFyJAq7I8xFdjjQtHXu62ToExCbQhBZDDeCaACtv'
OUT_FILE = '.'.join((sys.argv[1], 'tsv'))
LKEY = 'demo1'


def authenticate(key, secret):
    twitter = Twython(key, secret, oauth_version=2)
    twitter = Twython(key, access_token=twitter.obtain_access_token())
    return twitter


def get_tweets(twitter, query, num):
    tweet_url = "https://api.twitter.com/1.1/search/tweets.json"
    constructed_url = twitter.construct_api_url(tweet_url, q=query, count=num)
    results = twitter.request(constructed_url)

    tailored_tweets = []

    for status in results["statuses"]:
        tweet = {}
        tweet["text"] = status["text"]
        tweet["created_at"] = status["created_at"]
        tailored_tweets.append(tweet)
    return tailored_tweets


twitter = authenticate(APP_KEY, APP_SECRET)
tweets = get_tweets(twitter, sys.argv[1], 100)

rpc_client = AsyncClient()

rmv_whitespace_map = dict((ord(char), None)
                          for char in string.whitespace.strip(" "))
for tweet in tweets:
    try:
        text = unicode(tweet['text'])
        text = re.sub('[^A-Za-z0-9\s]+', '', text).strip()
        text = text.translate(rmv_whitespace_map)
        text = re.sub('[ ]+', ' ', text)
        ts = re.sub(':', '', tweet['created_at'])
        rpc_client.call(LKEY, text, ts, OUT_FILE)
    except:
        print 'Found a problem'
        pass

rpc_client.call(LKEY, "EOF", '', OUT_FILE)
