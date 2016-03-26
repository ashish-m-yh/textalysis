from tornado import web
import tornado

from async_client import AsyncClient
from xlread import xlread

import re
import os
import uuid
import xlwt

from operator import itemgetter, attrgetter

import sys
import conf


class Live(web.RequestHandler):
    def post(self):
        matrix = []
        pos_val = 0
        neg_val = 0
        ncols = 0

        lkey = self.get_argument("lkey")
        lkey_file = conf.lkey_dir + "/" + lkey + ".key"

        allowed = False
        limit = 0
        used = 0

        tmpfile = ""

        report_name = self.get_argument("report_name")

        if report_name.strip():
            tmpfile = report_name + "-" + str(uuid.uuid4()) + ".tsv"
        else:
            tmpfile = str(uuid.uuid4()) + ".tsv"

        if lkey != conf.demo_key:
            if not os.path.exists(lkey_file):
                self.render("./error_output.html",
                            errormsg="Invalid licence key entered",
                            infomsg="",
                            lkey="",
                            demo_key="",
                            tmpfile="")
            else:
                with open(lkey_file, "r") as out:
                    mystr = out.read()
                    out.close()

                    parts = mystr.split("\t")

                    used = int(parts[0])
                    limit = int(parts[2])

                    if (used >= limit):
                        self.render("./error_output.html",
                                    errormsg="Licence expired",
                                    infomsg="",
                                    lkey="",
                                    demo_key="",
                                    tmpfile="")
                    else:
                        allowed = True
        else:
            allowed = True

        if allowed and self.request.files.has_key('comment_file'):
            finfo = self.request.files['comment_file'][0]
            filename = conf.temp_dir + "/" + finfo['filename']

            if (re.search("excel|spreadsheet", finfo["content_type"])):
                with open(filename, "wb") as out:
                    out.write(bytes(finfo['body']))
                    out.close()

                try:
                    (matrix, ncols) = xlread(filename)
                except:
                    errormsg = 'Expecting a XLS (not XLSX) file. '
                    self.render("./error_output.html",
                                errormsg=errormsg,
                                infomsg="",
                                lkey="",
                                demo_key="",
                                tmpfile="")

                os.remove(filename)
            else:
                errormsg = 'Expecting a XLS (not XLSX) file. '
                self.render("./error_output.html",
                            errormsg=errormsg,
                            infomsg="",
                            lkey="",
                            demo_key="",
                            tmpfile="")
                allowed = False

            maxrows = len(matrix) / (ncols + 1)
            last_col = 0

            if lkey == conf.demo_key:
                maxrows = conf.demo_max_rows

            if (lkey != conf.demo_key and
                ((used + maxrows) > limit) and allowed):
                errormsg = "You have " + str(
                    limit) + " allowed accesses of which you have used " + str(
                        used) + ". You have " + str(
                            limit -
                            used) + " accesses remaining but your data set has " + str(
                                len(matrix)) + " rows. Therefore, please renew your licence to process this data set."
                self.render("./error_output.html",
                            errormsg=errormsg,
                            infomsg="",
                            lkey="",
                            demo_key=conf.demo_key,
                            tmpfile=tmpfile)
            elif allowed:
                infomsg = "Please check the Reports page for your completed report in a while."
                rpc_client = AsyncClient()

                rowcount = 0
                cust_agent = ""

                for rn in xrange(0, maxrows):
                    if (last_col >= len(matrix)):
                        break

                    r = matrix[last_col:last_col + ncols + 1]

                    try:
                        cust_agent = r.pop(0)
                        comment = r.pop(0)
                    except:
                        errormsg = "There may have been problems reading some rows due to corrupted or incomplete data."
                        pass

                    print lkey, cust_agent, comment

                    if comment is not None and comment.strip():
                        rpc_client.call(lkey, comment, cust_agent, tmpfile)
                    else:
                        errormsg = "Comments may not be available for all rows or there may be issues with reading the file format."

                    last_col = last_col + ncols + 1

                    rowcount = rowcount + 1

                rpc_client.call(lkey, "EOF", cust_agent, tmpfile)

                self.render("./error_output.html",
                            errormsg="",
                            infomsg=infomsg,
                            lkey=lkey,
                            demo_key=conf.demo_key,
                            tmpfile=tmpfile)
