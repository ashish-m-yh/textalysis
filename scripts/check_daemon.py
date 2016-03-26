#!/usr/bin/python

import subprocess
import os

p = subprocess.Popen('ps -eF | grep jsvc | grep -v grep', shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
retval = p.wait()

if retval == 1:
    os.chdir("/home/ta")
    call([ "./start.sh" ]) 
    print "Java daemon started" 
