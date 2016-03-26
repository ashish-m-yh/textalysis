import pika
import uuid


class AsyncClient():
    def __init__(self):
        QUEUE_NAME = 'rpc_queue'
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(
            host='localhost'))
        self.channel = self.connection.channel()
        self.channel.queue_declare(queue=QUEUE_NAME, durable=False)

    def call(self, lkey, msg, cust, filename):
        QUEUE_NAME = 'rpc_queue'
        q_entry = ":".join([lkey, cust, filename, msg])
        return self.channel.basic_publish(
            exchange='',
            routing_key=QUEUE_NAME,
            properties=pika.BasicProperties(delivery_mode=2),
            body=q_entry)

    def close():
        self.connection.close()
