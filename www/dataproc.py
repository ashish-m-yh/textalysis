from tornado import web
from os import path
import tornado
from operator import itemgetter


class Dataproc(web.RequestHandler):
    def post(self):
        rows = []

        try:
            finfo = self.request.files['comments'][0]
            file_key = self.get_argument('file_key')

            filename = '/var/www/reports/' + file_key + '.txt'
            rows = finfo['body']
            f = open(filename, 'w')
            f.write(str(rows))
            f.close()

            self.write('File uploaded successfully with key - ' + file_key)
        except:
            self.clear()
            self.set_status(400)
            self.finish(
                'There was an unexpected error. Please check your request format.')

    def get(self, file_key):
        try:
            file = path.join('./reports/prod/',
                             '.'.join((file_key, 'txt.tsv')))
            record = ['created_at', 'comment', 'score', 'pos_per', 'neg_per',
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
