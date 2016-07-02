from tornado import web
import tornado


class Dataproc(web.RequestHandler):
    def post(self):
        rows = []

        try:
            finfo = self.request.files['comments'][0]
            file_key = self.get_argument('file_key')
             
            filename = '/var/www/reports/' + file_key + '.txt' 
            rows = finfo['body']
            f = open(filename, 'w')
            f.write(str(rows))
            f.close()

            self.write('File uploaded successfully with key - ' + file_key)
        except:
            self.clear()
            self.set_status(400)
            self.finish('There was an unexpected error. Please check your request format.')
