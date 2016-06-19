# README #

## Overview

This is the Textalysis project for customer feedback analysis based on sentiment analysis techniques

## Dependencies

* Java 6 - 8
* Python 2.7
* Apache 2.4.x
* Supervisor
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

* Create a symlink for compile_java.sh in nlp_server folder
  ```ln -s ./scripts/compile_java.sh ./nlp_server/compile_java.sh```
* ```cd nlp_server```
* Run ./compile_java.sh to compile *.java files
* Run ./start.sh to start the NLP server

### For Python server ###

* pip install -r scripts/requirements.txt
* ```cd www``` && ```python server.py```
* ```mkdir /var/www/excel```
* ```mkdir -p /var/www/reports/demo1```
* set user variable in ./config/pythonserver.py to your username
* set directory variable to ./www folder
* Place ./config/pythonserver.py in /etc/supervisor/conf.d
* ```sudo supervisorctl restart all```

### For Apache Server ###

* Change DocumentRoot in ./config/000-default.conf
* Place ./config/000-default.conf in /etc/apache2/sites-available
* For directories /var/www/excel and /var/www/reports change the following:
    1. Ownership: to user running the python server
        * ```sudo chown -R <user>: excel```
        * ```sudo chown -R <user>: reports```
    2. Permission: 700
        * ```chmod -R 755 excel```
        * ```chmod -R 755 reports```
* Restart apache server ```sudo service apache2 restart```

### Steps to ensure NLP server is running ###

1. ```sudo less /var/log/rabbit.err``` -> Daemon loaded successfully java_load done
2. ```sudo less /var/log/rabbit.log``` -> initializing... Awaiting RPC requests
3. check if jsvc and rabbitmq processes are running
    * ```ps -ef | grep -i jsvc```
    * ```ps -ef | grep -i rabbit```
4. ```sudo rabbitmqctl list_queues``` -> Listing queues ... rpc_queue   0

### Steps to ensure Python server is running ###

1. ```lynx http://localhost:8080/app/test``` should display test page
2. ```sudo supervisorctl status``` -> Display textalysis_webapp program as RUNNING

### Steps to ensure Apache server is running ###

* [localhost](http://localhost) should display Textalysis website.

### Integration Test ###

* To ensure python server and NLP server is running:

    ```python www/twitter_srch.py``` should create a tsv file in /var/www/reports/demo1

* [Getting Started](http://localhost/start.html). Enter a twitter handle and a demo report will be shown.

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
