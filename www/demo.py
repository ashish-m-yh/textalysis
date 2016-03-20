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

class Demo(web.RequestHandler):
	def post(self):
		try:
			rows  = []
			pos_val = 0
			neg_val = 0

			try:
				comments = self.get_argument("comments")

				if (comments.strip()):
					body = comments
					rows  = comments.split("\n")
			except:
				pass

			try:
				body    = self.get_argument("body")
				pos_val = self.get_argument('pos_val')
				neg_val = self.get_argument('neg_val')
				rows  = body.split("\n")
			except:
				pass

			lkey = conf.demo_key 
			rows = rows[0:5] 

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

							pos  = int(agents[cust_agent][0])
							neg  = int(agents[cust_agent][1])

							agents[cust_agent] = [ pos + int(this_row[1]), neg + int(this_row[2]), my_ctr ]
						else:
							agents[cust_agent] = [ int(this_row[1]), int(this_row[2]), 1 ]

					if (int(this_row[1]) >= int(pos_val) and int(pos_val) != 0):
						pos_ctr = pos_ctr + 1

					if (int(this_row[2]) >= int(neg_val) and int(neg_val) != 0):
						neg_ctr = neg_ctr + 1

			output = sorted(output, key=itemgetter(1), reverse=True) 

			tmpfile  = str(uuid.uuid4()) + ".xls" 
			rowcount = 1

			book   	= xlwt.Workbook(encoding="utf-8")
			sheet1 	= book.add_sheet("Sheet 1")

			sheet1.write(0,0,"Analysis by User Comment") 
			sheet1.write(rowcount,0,"Agent")
			sheet1.write(rowcount,1,"Comment")
			sheet1.write(rowcount,2,"Sentiment score")
			sheet1.write(rowcount,3,"Positive %")
			sheet1.write(rowcount,4,"Negative %")

			for r in output:
				rowcount = rowcount + 1
				sheet1.write(rowcount,0,r[4])
				sheet1.write(rowcount,1,r[0])
				sheet1.write(rowcount,2,str(r[1]))
				sheet1.write(rowcount,3,str(r[2]))
				sheet1.write(rowcount,4,str(r[3]))

			rowcount = rowcount + 2
			sheet1.write(rowcount,0,"The sentiment score reflects quantum of sentiment contained in the text. Zero is indication of a neutral comment.")

			rowcount = rowcount + 1
			sheet1.write(rowcount,0,"The percentages represent the mix of sentiment in the same comment.")

			rowcount = rowcount + 1
			sheet1.write(rowcount,0,"")

			rowcount = 0

			sheet2 	= book.add_sheet("Sheet 2")
			sheet2.write(rowcount,0,"Analysis by Agent")

			rowcount = rowcount + 1
			sheet2.write(rowcount,0,"Agent Email")
			sheet2.write(rowcount,1,"% Positive")
			sheet2.write(rowcount,2,"% Negative")
			sheet2.write(rowcount,3,"Cases handled")

			for r in agents.keys():
				rowcount = rowcount + 1

				pos  = int(agents[r][0])
				neg  = int(agents[r][1])
				tot  = pos + neg

				if (tot != 0):
					pos_pc = pos*100/tot;
					neg_pc = neg*100/tot;    
				else:
					pos_pc = 0
					neg_pc = 0

				agents[r][0] = pos_pc
				agents[r][1] = neg_pc

				sheet2.write(rowcount,0,r)
				sheet2.write(rowcount,1,pos_pc)
				sheet2.write(rowcount,2,neg_pc)
				sheet2.write(rowcount,3,agents[r][2])

			book.save("./excel/" + tmpfile)

			self.render("./demo_output.html", lkey=lkey, body=body, tuples=output, pos_ctr=pos_ctr, neg_ctr=neg_ctr, pos_val=pos_val, neg_val=neg_val, reporturl="/excel/" + tmpfile, agents=agents)
		except:
			self.write("There was an error due to one of the following reasons - ")
			self.write("<ul><li>Your licence key has expired or invalid</li>")
			self.write("<li>You uploaded a file of a wrong input format</li></ul>")
