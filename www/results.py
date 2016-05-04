from os import path
from tornado import web
import tornado


class Results(web.RequestHandler):
    def get(self, twttr_handle):
        file = path.join('./reports/demo1/', '.'.join((twttr_handle, 'tsv')))
        record = ['created_at', 'tweet', 'score', 'pos_per', 'neg_per',
                  'status', 'category', 'issue']
        with open(file, 'r') as report:
            results = [dict(zip(record, line.strip().split('\t')))
                       for line in report]
            self.write(dict(results=results))
