from tornado import web
import tornado

class Test(web.RequestHandler):
	def get(self):
		self.write("test page")
		self.finish()
