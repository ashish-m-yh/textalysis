from tornado import web
import tornado

from operator import itemgetter

from async_client import AsyncClient

import uuid
import re
import datetime
import os
import time

class Analyze(web.RequestHandler):
    def post(self):
        try:
            LKEY = 'prod'
            comment = self.get_argument('comment')
            comment = comment.rstrip()
            out_file = str(uuid.uuid4()) + '.tsv'

            ts = re.sub(':', '', str(datetime.datetime.now()))
            ts = re.sub(' ', '', ts).split('.')[0]
            ts = ts[:10] + ' ' + ts[10:]

            rpc_client = AsyncClient()
            rpc_client.call(LKEY, comment, 'now', out_file)
            rpc_client.call(LKEY, "EOF", '', out_file)

            file = './reports/prod/' + out_file
            record = ['created_at', 'comment', 'score', 'pos_per', 'neg_per',
                      'status', 'category', 'issue']

            time.sleep(5)

            with open(file, 'r') as report:
                results = [dict(zip(record, line.strip().split('\t')))
                           for line in report]
                results = sorted(results,
                                 key=itemgetter('score'),
                                 reverse=True)
                self.write(dict(results=results))
        except Exception, e:
            self.clear()
            self.set_status(400)
            self.finish(
                'There was an unexpected error. Please check your request format.' + str(e))
