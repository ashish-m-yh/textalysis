from os import path
from tornado import web
from operator import itemgetter
import tornado, re
import conf

class Results(web.RequestHandler):
    def get(self, twttr_handle):
        try:
            path_prefix = './' + conf.report_dir + '/' + conf.demo_key + '/'

            file = path.join(path_prefix, '.'.join((twttr_handle, 'tsv')))
            record = ['created_at', 'tweet_id', 'tweet', 'score', 'pos_per', 'neg_per',
                      'status', 'category', 'issue']

            with open(file, 'r') as report:
                results = [dict(zip(record, re.split(r'[/\t]', line.strip())))
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
