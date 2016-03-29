# Copy jar files to specified location:
#	
#	rabbitmq-client.jar -> textalysis/nlp_server
# 	opennlp-tools-1.5.3.jar -> textalysis/nlp_server/lib
# 	slf4j-api-1.6.4.jar -> /usr/share/java
#	slf4j-simple-1.6.4.jar -> /usr/share/java
# 	commons-daemon.jar -> /usr/share/java
#	
# Run following from textalysis/nlp_server

javac ./dev/com/efficientmail/Meter.java
javac ./dev/com/efficientmail/TextClean.java
javac -cp ./lib/opennlp-tools-1.5.3.jar ./dev/com/efficientmail/SentiClassifyLoader.java
javac -cp ./lib/opennlp-tools-1.5.3.jar:/usr/share/java/slf4j-api-1.6.4.jar:./dev ./dev/com/efficientmail/SentiClassify.java
javac -cp ./dev:/usr/share/java/slf4j-api-1.6.4.jar ./dev/com/efficientmail/ThemeDetect.java
javac -cp ./rabbitmq-client.jar:./dev:/usr/share/java/commons-daemon.jar RabbitServer.java