from tornado import web
import tornado

from rabbitclient import RpcClient
from xlread import xlread

import re
import os
import uuid
import xlwt

from operator import itemgetter, attrgetter

import conf

class Analyzer(web.RequestHandler):
	def post(self):
		try:
			rows  = []
			finfo = {} 
			pos_val = 0
			neg_val = 0

			finfo['body'] = ''

			wrong_mime = False
			mode	   = ""

			try:
				mode = self.get_argument("mode")
			except:
				pass

			try:
				comments = self.get_argument("comments")
				if (mode != "email"):
					mode="commentlist"

				if (comments.strip()):
					finfo['body'] = comments
					rows  = comments.split("\n")
			except:
				pass

			if self.request.files.has_key('comment_file'):
				finfo 	 = self.request.files['comment_file'][0]
				filename = conf.temp_dir + "/" + finfo['filename'] 
				mode	 = "commentfile"		
		
				if (finfo['content_type'] == "text/plain"):
					rows = finfo['body'].split("\n")
				elif (re.search("excel|spreadsheet",finfo["content_type"])):
					with open (filename, "wb") as out:
						out.write(bytes(finfo['body']))
						out.close()

					try:
						matrix = xlread(filename)
					except:
						self.write('Expecting a plain text or XLS (not XLSX) file. <a href="javascript: history.go(-1)">Go back</a>')
						wrong_mime = True
	
					for r in matrix:
						if (isinstance(r,list)):
							for x in r:
								rows.append(x)
			
					os.remove(filename)
				else:
					self.write('Expecting a plain text or XLS (not XLSX) file. <a href="javascript: history.go(-1)">Go back</a>')
					wrong_mime = True

			try:
				body    = self.get_argument("body")
				pos_val = self.get_argument('pos_val')
				neg_val = self.get_argument('neg_val')
				finfo['body'] = body
				rows  = body.split("\n")
			except:
				pass

			lkey = self.get_arguments("lkey")
						
			if (len(lkey) > 0):
				lkey = lkey[0]
			else:
				lkey = conf.demo_key 
				rows = rows[0:10] 

			rpc_client = RpcClient()

			output = []

			pos_ctr = 0
			neg_ctr = 0
	
			agents    = dict()

			for r in rows:
				if (r.strip()):
					cust_agent = r.split("/").pop(0)
					r		   = re.sub(cust_agent+"/", '', r)

					response 	= rpc_client.call(lkey,r)
					if (response == 'Licence expired or key not provided'):
						raise Exception("Licence expired") 

					this_row 	= response.split(" ")
					this_row[0] = round(float(this_row[0]),2)

					if (int(this_row[1]) >= int(pos_val) or int(this_row[2]) >= int(neg_val)):
						output.append([ r, this_row[0], this_row[1], this_row[2], cust_agent ])	

						if (agents.has_key(cust_agent)):
							my_ctr = agents[cust_agent][2] + 1
							agents[cust_agent] = [ (int(agents[cust_agent][0]) + int(this_row[1]))/my_ctr, (int(agents[cust_agent][1]) + int(this_row[2]))/my_ctr, my_ctr ]
						else:
							agents[cust_agent] = [ int(this_row[1]), int(this_row[2]), 1 ]

#						if (agents[cust_agent][0] + agents[cust_agent][1] < 100):
#							agents[cust_agent][0] = agents[cust_agent][0] + 1 

					if (int(this_row[1]) >= int(pos_val) and int(pos_val) != 0):
						pos_ctr = pos_ctr + 1

					if (int(this_row[2]) >= int(neg_val) and int(neg_val) != 0):
						neg_ctr = neg_ctr + 1

			output = sorted(output, key=itemgetter(1), reverse=True) 

			tmpfile  = str(uuid.uuid4()) + ".xls" 
			rowcount = 1

			book   	= xlwt.Workbook(encoding="utf-8")
			sheet1 	= book.add_sheet("Sheet 1")

			sheet1.write(0,0,"Comment")
			sheet1.write(0,1,"Sentiment score")
			sheet1.write(0,2,"Positive %")
			sheet1.write(0,3,"Negative %")

			for r in output:
				sheet1.write(rowcount,0,r[0])
				sheet1.write(rowcount,1,str(r[1]))
				sheet1.write(rowcount,2,str(r[2]))
				sheet1.write(rowcount,3,str(r[3]))
				rowcount = rowcount + 1

			rowcount = rowcount + 1
			sheet1.write(rowcount,0,"The sentiment score reflects quantum of sentiment contained in the text. Zero is indication of a neutral comment.")

			rowcount = rowcount + 1
			sheet1.write(rowcount,0,"The percentages represent the mix of sentiment in the same comment.")

			book.save("./excel/" + tmpfile)

			if not wrong_mime:
				if (mode == "email"):
					self.render("./demo_output.html", lkey=lkey, body=finfo['body'], tuples=output, pos_ctr=pos_ctr, neg_ctr=neg_ctr, pos_val=pos_val, neg_val=neg_val, reporturl="/excel/" + tmpfile, agents=agents)
				else:
					self.render("./prod_output.html", reporturl="/excel/" + tmpfile, lkey=lkey, body=finfo['body'])
		except:
			self.write("There was an error due to one of the following reasons - ")
			self.write("<ul><li>Your licence key has expired or invalid</li>")
			self.write("<li>You uploaded a file of a wrong input format</li></ul>")
