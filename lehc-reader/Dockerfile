FROM maven:3.8-openjdk-8-slim

WORKDIR /app

ADD pom.xml /app/pom.xml

ADD local-maven-repo /app/local-maven-repo

ADD src /app/src

RUN mvn clean compile

CMD mvn exec:java -Dexec.mainClass=com.numaolab.App