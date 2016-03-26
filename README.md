# README #

## Overview

This is the Textalysis project for customer feedback analysis based on sentiment analysis techniques

## Dependencies

* Java 6
* Python 2.7
* RabbitMQ
* opennlp-tools-1.5.3.jar
* slf4j-api-1.6.4.jar
* slf4j-simple-1.6.4.jar
* rabbitmq-client.jar
* commons-daemon.jar

## Setup

* Check out the code
* Install the www code in /var/www 
* cd /var/www && python server.py
* Run ./start.sh to start the NLP server
* mkdir /var/www/excel
* mkdir -p /var/www/reports/demo1

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