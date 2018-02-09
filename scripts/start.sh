#!/bin/bash

cd /home/ta/production/textalysis/nlp_server

/usr/bin/jsvc -cp $PWD/libs/opennlp-tools-1.5.3.jar:$PWD/libs/opennlp-maxent-3.0.1.jar:$PWD/dev:$PWD/libs/slf4j-api-1.6.4.jar:$PWD/libs/slf4j-simple-1.6.4.jar:$PWD/libs/rabbitmq-client.jar:$PWD/libs/commons-daemon.jar:$PWD -java-home /usr/lib/jvm/java-6-openjdk-i386 -Dmeter_dir=/var/www -debug -pidfile /var/run/rabbitserver.pid -outfile /var/log/rabbit.log -errfile /var/log/rabbit.err RabbitServer
