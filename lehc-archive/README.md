```
mvn archetype:generate \
      -DarchetypeGroupId=org.apache.beam \
      -DarchetypeArtifactId=beam-sdks-java-maven-archetypes-examples \
      -DarchetypeVersion=2.33.0 \
      -DgroupId=org.example \
      -DartifactId=word-count-beam \
      -Dversion="0.1" \
      -Dpackage=org.apache.beam.examples \
      -DinteractiveMode=false
```

```
mvn compile exec:java -Dexec.mainClass=org.apache.beam.examples.Main \
     -Dexec.args="--inputFile=sample.txt --output=counts" -Pdirect-runner
```

```
mvn clean package -Pflink-runner
```

```
mongo "mongodb://root:example@localhost:27017/"
db.createCollection("capped", {capped: true, size:1000})
```

```
mvn compile exec:java -Dexec.mainClass=org.apache.beam.examples.MQTTtoSTDOUT \
      -Dexec.args="--inputTopic=all" -Pdirect-runner
```

```
mvn compile exec:java -Dexec.mainClass=org.apache.beam.examples.MQTTtoSTDOUT \
      -Dexec.args="--runner=FlinkRunner --inputTopic=all" -Pflink-runner
```

```
org.apache.beam.examples.MQTTtoSTDOUT
--runner=FlinkRunner --MQTTHost=mosquitto --inputTopic=all
```

```
org.apache.beam.examples.MQTTtoMongoDB
--runner=FlinkRunner --MQTTHost=mosquitto --DBHost=mongo --DBName=test --colName=log --inputTopic=all --checkpointingInterval=1000
```
