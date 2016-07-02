from async_client import AsyncClient

import glob
import sys
import datetime
import re
import os

LKEY = 'prod'

input_files = glob.glob('/var/www/reports/*.txt')

rpc_client = AsyncClient()

for txt_file in input_files:
    out_file = (txt_file + '.tsv').split("/")[-1]

    try:
        f = open(txt_file, "r")
        for line in f.readlines():
            line = line.rstrip()
            ts = re.sub(':', '', str(datetime.datetime.now()))
            ts = re.sub(' ', '', ts).split('.')[0]
            ts = ts[:10] + ' ' + ts[10:]
            rpc_client.call(LKEY, line, ts, out_file)
        f.close()

        os.remove(txt_file)

        rpc_client.call(LKEY, "EOF", '', out_file)
    except:
        pass
