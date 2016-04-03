# README #

## Overview

This is the Textalysis project for customer feedback analysis based on sentiment analysis techniques

## Dependencies

* Java 6
* Python 2.7
* Apache 2.4.7
* RabbitMQ
* opennlp-tools-1.5.3.jar
* opennlp-maxent-3.0.1.jar
* slf4j-api-1.6.4.jar
* slf4j-simple-1.6.4.jar
* rabbitmq-client.jar
* commons-daemon.jar

## Installation Steps

* Check out the code repo

### For NLP server ###

* Run ./compile_java.sh to compile *.java files
* Run ./start.sh to start the NLP server

### For python server ###

* Place the www code in /var/www 
* pip install -r scripts/requirements.txt
* cd /var/www && python server.py
* mkdir /var/www/excel
* mkdir -p /var/www/reports/demo1

### For Apache Server ###

* Place ./config/000-default.conf in /etc/apache2/sites-available
* ```chown -R <user>: excel```
* ```chown -R <user>: reports```
* ```sudo service apache2 restart```

### Steps to ensure NLP server is running ###

1. ```sudo less /var/log/rabbit.err``` -> Daemon loaded successfully java_load done
2. ```sudo less /var/log/rabbit.log``` -> initializing... Awaiting RPC requests
3. check if jsvc and rabbitmq processes are running
    * ```ps -ef | grep -i jsvc```
    * ```ps -ef | grep -i rabbit```
4. ```sudo rabbitmqctl list_queues``` -> Listing queues ... rpc_queue   0

### Steps to ensure Apache server is running ###

* [localhost](http://localhost) should display Textalysis website.

## Configuration

* Set the values in /var/www/conf.py

### Contribution guidelines ###

* git checkout -b <branch>
* git push -u origin <branch>
* git commit -m "<message>" <files>
* git push
* Create a Pull Request

### Who do I talk to? ###

* Mail Ashish Mukherjee (ashish.mukherjee@gmail.com)
