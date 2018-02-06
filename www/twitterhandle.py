from tornado import web
import tornado, json, os
import subprocess


class TwitterHandle(web.RequestHandler):
    def get(self):
        try:
            data = []
            os.chmod('./available_handle.txt', 436)
            with open('available_handle.txt', 'r') as handles:
                data = handles.read().splitlines()
            self.write(json.dumps(data))
        except:
            self.clear()
            self.set_status(400)
            self.finish('Handles cannot be loaded.')

    def post(self):
        try:
            handle = self.get_argument('handle').encode('utf-8')
            command = './twttr_cron.sh ' + handle
            subprocess.call(command, shell=True)
            allHandles = []
            with open('./available_handle.txt', 'r+') as handles:
                exists = False
                for line in handles:
                    allHandles.append(line.strip())
                    if line.strip() == handle:
                        exists = True
                        break
                if not exists:
                    allHandles.append(handle)
                    handles.write(handle + '\n')
            self.write(json.dumps(allHandles))
        except:
            self.clear()
            self.set_status(400)
            self.finish('Custom Handle cannot be added.')
