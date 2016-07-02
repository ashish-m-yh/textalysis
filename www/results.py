from os import path
from tornado import web
from operator import itemgetter
import tornado


class Results(web.RequestHandler):
    def get(self, twttr_handle):
        try:
            file = path.join('./reports/demo1/',
                             '.'.join((twttr_handle, 'tsv')))
            record = ['created_at', 'tweet', 'score', 'pos_per', 'neg_per',
                      'status', 'category', 'issue']

            with open(file, 'r') as report:
                results = [dict(zip(record, line.strip().split('\t')))
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
