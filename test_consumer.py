#!/usr/bin/env python
import pika
import time

credentials = pika.PlainCredentials('framebot', 'framebot')
parameters = pika.ConnectionParameters(
    host='192.168.2.4',
    virtual_host="/metadata",
    port=5672,
    credentials=credentials
)
connection = pika.BlockingConnection(parameters)

channel = connection.channel()

channel.queue_declare(queue='metadata-queue', durable=True)

def callback(ch, method, properties, body):
    print("%r" % body)
    time.sleep(body.count(b'.'))
    ch.basic_ack(delivery_tag = method.delivery_tag)

channel.basic_qos(prefetch_count=1)
channel.basic_consume(callback,
                      queue='metadata-queue')

channel.start_consuming()
