export CLASSPATH="./libs/commons-daemon.jar:./libs/opennlp-maxent-3.0.1.jar:./libs/opennlp-tools-1.5.3.jar:./libs/rabbitmq-client.jar:./libs/slf4j-api-1.6.4.jar:./libs/slf4j-simple-1.6.4.jar:./dev"

for i in `echo ./dev/com/efficientmail/*.java RabbitServer.java`
do
  javac $i
done