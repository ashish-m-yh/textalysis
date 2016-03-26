#!/usr/bin/python

import httplib
import os, signal

from subprocess import call

status = None

try:
	conn = httplib.HTTPConnection("localhost",8080)
	conn.request("GET","/app/test")
	response = conn.getresponse()
	status   = response.status
except:
	pass

if status != 200:
	os.chdir("/var/www")

	mypid = 0
	
	try:
		f = open("/var/run/server.pid","r")
		mypid = int(f.read())
		f.close()	
	except:
		pass

	try:
		if int(mypid) > 1:
			os.kill(mypid,signal.SIGQUIT)
			os.remove("/var/run/server.pid")
	except:
		pass

	print "server restarted"

	call([ "python", "server.py" ])
