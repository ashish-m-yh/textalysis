import pika
import uuid

connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
channel    = connection.channel()
channel.queue_delete(queue='rpc_queue')
connection.close()
