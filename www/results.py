from os import path
from tornado import web
from operator import itemgetter
import tornado, re


class Results(web.RequestHandler):
    def get(self, twttr_handle):
        try:
            file = path.join('./reports/demo1/',
                             '.'.join((twttr_handle, 'tsv')))
            record = ['created_at', 'tweet_id', 'tweet', 'score', 'pos_per', 'neg_per',
                      'status', 'category', 'issue']

            with open(file, 'r') as report:
                results = [dict(zip(record, re.split(r'[/\t]', line.strip())))
                           for line in report]
                results = sorted(results,
                                 key=itemgetter('score'),
                                 reverse=True)
                self.write(dict(results=results))
        except:
            self.clear()
            self.set_status(400)
            self.finish(
                'There was an unexpected error. Please check your request format.')
