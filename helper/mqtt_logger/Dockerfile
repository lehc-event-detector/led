FROM maven:3.8-openjdk-8

WORKDIR /app

ADD ./pom.xml /app
ADD ./src /app/src

CMD mvn compile exec:java -Dexec.mainClass=org.apache.beam.examples.Main  -Pdirect-runner