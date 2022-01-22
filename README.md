```
mvn exec:java -Dexec.mainClass=org.apache.beam.examples.Main \
    -Pflink-runner \
    -Dexec.args="--runner=FlinkRunner \
      --host=mosquitto \
      --port=1883 \
      --inputTopic=test \
      --outputTopic=ans \
      --flinkMaster=<flink master url> \
      --filesToStage=target/word-count-beam-bundled-0.1.jar"
```

# DBにCLIで接続する方法

1. `docker exec -it <mongoのコンテナ> bash`
2. `mongo mongodb://root:example@localhost:27017`

# caped collection
- 1073741824byte = 1gb
db.createCollection("log", { capped: true, size: 1073741824 } )

mvn archetype:generate \
      -DarchetypeGroupId=org.apache.beam \
      -DarchetypeArtifactId=beam-sdks-java-maven-archetypes-examples \
      -DarchetypeVersion=2.33.0 \
      -DgroupId=com.numaolab \
      -DartifactId=lehc-event-detector \
      -Dversion="1.0" \
      -DinteractiveMode=false

mvn archetype:generate \
      -DarchetypeGroupId=org.apache.beam \
      -DarchetypeArtifactId=beam-sdks-java-maven-archetypes-examples \
      -DarchetypeVersion=2.33.0 \
      -DgroupId=org.example \
      -DartifactId=word-count-beam \
      -Dversion="0.1" \
      -Dpackage=org.apache.beam.examples \
      -DinteractiveMode=false

mvn archetype:generate \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false \
  -DgroupId=com.example \
  -DartifactId=HelloWorld