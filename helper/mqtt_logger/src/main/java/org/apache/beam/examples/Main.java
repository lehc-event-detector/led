package org.apache.beam.examples;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.mqtt.MqttIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

public class Main {

  public interface MainOptions extends PipelineOptions {
    @Description("MQTT HOST")
    @Default.String("localhost")
    String getHost();
    void setHost(String value);

    @Description("MQTT PORT")
    @Default.String("1883")
    String getPort();
    void setPort(String value);

    @Description("INPUT TOPIC")
    @Default.String("test")
    String getInputTopic();
    void setInputTopic(String value);

    @Description("OUTPUT TOPIC")
    @Default.String("ans")
    String getOutputTopic();
    void setOutputTopic(String value);
  }

  public static void main(String[] args) {
    MainOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MainOptions.class);

    String mqtt_address = "tcp://" + options.getHost() + ":" + options.getPort();
    String input_topic = options.getInputTopic();
    String output_topic = options.getOutputTopic();

    Pipeline pipeline = Pipeline.create(options);

    pipeline
      /*
      ** Receive messages
      */
      .apply(
        MqttIO.read().withConnectionConfiguration(
          MqttIO.ConnectionConfiguration.create(mqtt_address, input_topic)))
      /*
      ** byte[] to String & Println
      */
      .apply(
        ParDo.of(
          new DoFn<byte[], String>() {
            @ProcessElement
            public void processElement(ProcessContext processContext) {
              byte[] element = processContext.element();
              // System.out.println(new String(element));
              processContext.output(new String(element));
            }
          }))
      /*
      ** String to byte[]
      */
      .apply(
        ParDo.of(
          new DoFn<String, byte[]>() {
            @ProcessElement
            public void processElement(ProcessContext processContext) {
              String element = processContext.element();
              processContext.output(element.getBytes());
            }
          }))
      /* send mqtt
      **
      */
      .apply(
        MqttIO.write().withConnectionConfiguration(
          MqttIO.ConnectionConfiguration.create(mqtt_address, output_topic)));

    pipeline.run().waitUntilFinish();
  }
}
