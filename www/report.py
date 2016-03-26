from tornado import web
import tornado

import re, os

import conf

import xlwt

from operator import itemgetter, attrgetter


class Report(web.RequestHandler):
    def on_finish(self):
        rpt_file = "./reports/" + self.lkey + "/" + self.filename
#		if os.path.isfile(rpt_file):
#			os.remove(rpt_file)

    def get(self, filename, lkey):
        self.filename = filename
        self.lkey = lkey

        matrix = []

        try:
            with open("./reports/" + lkey + "/" + filename, "r") as out:
                matrix = out.readlines()
                out.close()
        except:
            self.render("./error_output.html",
                        infomsg="Report not found.",
                        errormsg="",
                        lkey="")
            return None

        output = []
        agents = dict()
        p_distrib = dict()
        n_distrib = dict()

        p_total = 0
        n_total = 0

        tmpfile = re.sub(r'.tsv', '.xls', filename)

        for r in matrix:
            r = r.strip()
            row = r.split("\t")
            cust_agent = row[0]

            row[2] = float(row[2])
            row[3] = int(row[3])
            row[4] = int(row[4])

            if len(row) >= 6:
                row[5] = re.sub(r',$', '', str(row[5]).strip())
                issues = row[5].split(",")

                for issue in issues:
                    issue = issue.strip()
                    if issue:
                        p_total = p_total + 1
                        if p_distrib.has_key(issue):
                            p_distrib[issue] = p_distrib[issue] + 1
                        else:
                            p_distrib[issue] = 1

            if len(row) >= 7:
                row[6] = re.sub(r',$', '', str(row[6]).strip())
                issues = row[6].split(",")

                for issue in issues:
                    issue = issue.strip()
                    if issue:
                        n_total = n_total + 1
                        if n_distrib.has_key(issue):
                            n_distrib[issue] = n_distrib[issue] + 1
                        else:
                            n_distrib[issue] = 1

            if len(row) >= 8:
                row[7] = re.sub(r',$', '', str(row[7]).strip())

            output.append(row)

            if (agents.has_key(cust_agent)):
                myctr = agents[cust_agent][2] + 1
                pos_tot = int(agents[cust_agent][0]) + int(row[3])
                neg_tot = int(agents[cust_agent][1]) + int(row[4])

                agents[cust_agent] = [pos_tot, neg_tot, myctr]
            else:
                agents[cust_agent] = [int(row[3]), int(row[4]), 1]

        output.sort(key=lambda x: x[2] * x[4] / 100, reverse=True)

        rowcount = 1

        book = xlwt.Workbook(encoding="utf-8")
        sheet1 = book.add_sheet("Analysis by comments")

        sheet1.write(0, 0, "Analysis by User Comment")
        sheet1.write(rowcount, 0, "Agent email")
        sheet1.write(rowcount, 1, "Comment")
        sheet1.write(rowcount, 2, "Sentiment score")
        sheet1.write(rowcount, 3, "Positive %")
        sheet1.write(rowcount, 4, "Negative %")
        sheet1.write(rowcount, 5, "Positive highlights")
        sheet1.write(rowcount, 6, "Negative highlights")
        sheet1.write(rowcount, 7, "Detected keywords")

        for r in output:
            rowcount = rowcount + 1
            colcount = 0

            for y in r:
                sheet1.write(rowcount, colcount, y)
                colcount = colcount + 1

            col_diff = 8 - len(r)

            for x in xrange(0, col_diff):
                sheet1.write(rowcount, colcount, "-")
                colcount = colcount + 1

        rowcount = rowcount + 2
        sheet1.write(
            rowcount, 0,
            "The sentiment score reflects quantum of sentiment contained in the text. Zero is indication of a neutral comment.")

        rowcount = rowcount + 1
        sheet1.write(
            rowcount, 0,
            "The percentages represent the mix of sentiment in the same comment.")

        rowcount = rowcount + 1
        sheet1.write(
            rowcount, 0,
            "The rows are ordered from most negative to least negative")

        rowcount = rowcount + 1
        sheet1.write(
            rowcount, 0,
            "Positive and negative highlights indicate the issues which the customer commented on")

        rowcount = 0

        sheet2 = book.add_sheet("Analysis by Agent")
        sheet2.write(rowcount, 0, "Analysis by Agent")

        rowcount = rowcount + 1
        sheet2.write(rowcount, 0, "Agent Email")
        sheet2.write(rowcount, 1, "% Positive")
        sheet2.write(rowcount, 2, "% Negative")
        sheet2.write(rowcount, 3, "Cases handled")

        agent_tuples = []

        for r in agents.keys():
            rowcount = rowcount + 1

            pos = int(agents[r][0])
            neg = int(agents[r][1])
            tot = pos + neg

            if (tot != 0):
                pos_pc = pos * 100 / tot
                neg_pc = neg * 100 / tot
            else:
                pos_pc = 0
                neg_pc = 0

            agent_tuples.append([r, pos_pc, neg_pc])

            sheet2.write(rowcount, 0, r)
            sheet2.write(rowcount, 1, pos_pc)
            sheet2.write(rowcount, 2, neg_pc)
            sheet2.write(rowcount, 3, agents[r][2])

        book.save("./excel/" + tmpfile)

        if lkey == conf.demo_key and re.search(r'/app/demoreport',
                                               self.request.uri):
            self.render("./demo_output.html",
                        reporturl="/excel/" + tmpfile,
                        negs=n_distrib,
                        pos=p_distrib,
                        p_total=p_total,
                        n_total=n_total,
                        agents=agent_tuples)
        else:
            self.render("./prod_output.html",
                        reporturl="/excel/" + tmpfile,
                        negs=n_distrib,
                        pos=p_distrib,
                        p_total=p_total,
                        n_total=n_total,
                        agents=agent_tuples)
