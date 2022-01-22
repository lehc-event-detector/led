package org.apache.beam.examples;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.mqtt.MqttIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;

public class MQTTtoSTDOUT {

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
  }

  static class ByteToString extends DoFn<byte[], String> {
    @ProcessElement
    public void process(ProcessContext processContext) {
      processContext.output(new String(processContext.element()));
    }
  }

  static class PrintEach<T> extends DoFn<T, T> {
    @ProcessElement
    public void process(@Element T r, OutputReceiver<T> out) {
      System.out.println(r);
      out.output(r);
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
     * Fixed time window
     * **********************************************************************************************
     */
    PCollection<byte[]> wdMessages =
        messages.apply(Window.<byte[]>into(FixedWindows.of(Duration.standardSeconds(3))));
    
    /**
     * **********************************************************************************************
     * byte[] to String
     * **********************************************************************************************
     */
    PCollection<String> jstr = wdMessages.apply(ParDo.of(new ByteToString()));

    /**
     * **********************************************************************************************
     * STDOUT
     * **********************************************************************************************
     */
    jstr.apply(ParDo.of(new PrintEach<String>()));

    p.run().waitUntilFinish();
  }

  public static void main(String[] args) {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

    runWordCount(options);
  }
}
