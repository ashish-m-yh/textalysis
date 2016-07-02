import daemon
from tornado import web, httpserver, ioloop

from live import Live
from report import Report
from reportlist import ReportList
from results import Results
from test import Test
from new_demo import NewDemo
from dataproc import Dataproc

if __name__ == "__main__":

    application = web.Application(
        [
            (r"/app/live", Live),
            (r'/app/reportlist', ReportList),
            (r'/app/report/(.+)/(.+)', Report),
            (r'/app/demoreport/(.+)/(.+)', Report),
            (r'/app/reports/(.+).json', Results),
            (r'/app/reports/demo', NewDemo),
            (r'/app/dataproc', Dataproc),
            (r'/app/dataproc/(.+).json', Dataproc),
            (r'/app/test', Test)
        ],
        debug=True)

    http_server = httpserver.HTTPServer(application)
    http_server.listen(8080)
    try:
        ioloop.IOLoop.instance().start()
    except:
        pass
