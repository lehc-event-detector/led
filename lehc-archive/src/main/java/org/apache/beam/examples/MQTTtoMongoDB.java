package org.apache.beam.examples;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.mongodb.MongoDbIO;
import org.apache.beam.sdk.io.mqtt.MqttIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.bson.Document;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;

public class MQTTtoMongoDB {

  public interface Options extends StreamingOptions {
    @Description("MQTT HOST")
    @Default.String("localhost")
    String getMQTTHost();
    void setMQTTHost(String value);

    @Description("MQTT PORT")
    @Default.String("1883")
    String getMQTTPort();
    void setMQTTPort(String value);

    @Description("INPUT TOPIC")
    @Default.String("test")
    String getInputTopic();
    void setInputTopic(String value);

    @Description("DB HOST")
    @Default.String("test")
    String getDBHost();
    void setDBHost(String value);

    @Description("DB NAME")
    @Default.String("test")
    String getDBName();
    void setDBName(String value);

    @Description("COL NAME")
    @Default.String("test")
    String getColName();
    void setColName(String value);
  }

  static class ByteToString extends DoFn<byte[], String> {
    @ProcessElement
    public void process(ProcessContext processContext) {
      processContext.output(new String(processContext.element()));
    }
  }

  static class StringToBSON extends DoFn<String, Document> {
    @ProcessElement
    public void process(ProcessContext processContext) {
      // processContext.output(Document.parse("{\"" + "name" + "\": " + "\"" + processContext.element() + "\"" + "}"));
      processContext.output(Document.parse(processContext.element()));
    }
  }

  static void runWordCount(Options options) {
    options.setStreaming(true);
    Pipeline p = Pipeline.create(options);

    /**
     * **********************************************************************************************
     * Receive messages
     * **********************************************************************************************
     */
    PCollection<byte[]> messages =
        p.apply(
          MqttIO.read().withConnectionConfiguration(
            MqttIO.ConnectionConfiguration.create("tcp://" + options.getMQTTHost() + ":" + options.getMQTTPort(), options.getInputTopic())));

    /**
     * **********************************************************************************************
     * byte[] to String
     * **********************************************************************************************
     */
    PCollection<String> jstr = messages.apply(ParDo.of(new ByteToString()));

    /**
     * **********************************************************************************************
     * String to BSON
     * **********************************************************************************************
     */
    PCollection<Document> docs = jstr.apply(ParDo.of(new StringToBSON()));

    /**
     * **********************************************************************************************
     * INSERT
     * **********************************************************************************************
     */
    docs.apply(MongoDbIO.write()
            .withUri("mongodb://root:example@" + options.getDBHost() + ":27017")
            .withDatabase(options.getDBName())
            .withCollection(options.getColName()));

    p.run().waitUntilFinish();
  }

  public static void main(String[] args) {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

    runWordCount(options);
  }
}
