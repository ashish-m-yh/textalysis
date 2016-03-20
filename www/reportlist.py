from tornado import web
import tornado

import conf
import os, time

def sorted_ls(path):
	mtime = lambda f: os.stat(os.path.join(path, f)).st_mtime
	return reversed(list(sorted(os.listdir(path), key=mtime)))

class ReportList(web.RequestHandler):
	def get(self):
		lkey    = conf.demo_key 
		self.list_view(lkey)

	def post(self):
		lkey = self.get_argument("l_key")
		self.list_view(lkey)
	
	def list_view(self,lkey):
		rep_dir = "./reports/" + lkey
		msg		= ""
		files	= []

		if not os.path.exists(rep_dir):
			msg = "The reports folder for this licence key does not exist or the licence key is invalid."
		else:
			files= [ f for f in sorted_ls(rep_dir + "/") if not f.endswith(".part") ]

			for i in xrange(len(files)):
				time_st  = time.strftime("%d %B, %Y", time.localtime(os.path.getmtime(rep_dir + "/" + files[i])))
				files[i] = [ files[i], time_st ]

			if msg == "" and len(files) == 0:
				msg = "There are no available reports now. Reports are only available when analysis is complete." 
		
		self.render("./rpt_list.html", replist=files, lkey=lkey, msg=msg, demo_key=conf.demo_key)
