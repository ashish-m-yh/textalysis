from tornado import web
from tornado import gen

TCP_IP = '127.0.0.1'
TCP_PORT = 4005
BUFFER_SIZE = 1024
MESSAGE = "Hello, World!"

import socket
import time

from threading import Thread
from functools import wraps

def run_async(func):
	@wraps(func)
	def async_func(*args, **kwargs):
		func_hl = Thread(target = func, args = args, kwargs = kwargs)
		func_hl.start()
		return func_hl

	return async_func

@run_async
def sleeper(self,arg,callback):
	time.sleep(4)
	self.write(arg)	
	callback('DONE')

class Test1(web.RequestHandler):
    @web.asynchronous
    @gen.coroutine
    def get(self):
        response = yield gen.Task(sleeper,self,'abc')
        self.write(response)
        self.finish()
