from os import path
from tornado import web
import tornado

class NewDemo(web.RequestHandler):
    def get(self):
        self.render('./twitter_demo.html')
