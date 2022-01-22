package com.numaolab;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

import com.numaolab.schemas.TagData;
import com.numaolab.transforms.ArchiveToMongoDB;
import com.numaolab.transforms.CMDDetector;
import com.numaolab.transforms.CleanUp;
import com.numaolab.transforms.EventDetector;
import com.numaolab.transforms.EventDetectorPerGroup;
import com.numaolab.transforms.EventDetectorPerLogic;
import com.numaolab.transforms.InputFromMqtt;
import com.numaolab.transforms.NewEventDetector;
import com.numaolab.transforms.OutputToMongoDB;
import com.numaolab.transforms.Print;

import java.time.Instant;
import java.util.Date;

import com.numaolab.schemas.Result;
public class Main {

  public interface MainOptions extends PipelineOptions {
    @Description("ID")
    @Default.String("")
    String getId();
    void setId(String value);

    @Description("INPUT URL")
    @Default.String("localhost:1883:all")
    String getInputUrl();
    void setInputUrl(String value);

    @Description("ARCHIVE URL")
    @Default.String("root:example@localhost:27017:lehc:log")
    String getArchiveUrl();
    void setArchiveUrl(String value);

    @Description("OUTPUT URL")
    @Default.String("root:example@localhost:27017:lehc:result")
    String getOutputUrl();
    void setOutputUrl(String value);

    @Description("ENV")
    @Default.String("")
    String getEnv();
    void setEnv(String value);

    @Description("HEADER")
    @Default.String("")
    String getHeader();
    void setHeader(String value);

    @Description("TYPE")
    @Default.String("0")
    String getType();
    void setType(String value);

    @Description("IS LOGGING")
    @Default.Boolean(true)
    Boolean getIsLogging();
    void setIsLogging(Boolean value);
  }

  public static void main(String[] args) {
    MainOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MainOptions.class);
    if (options.getId().equals("")) {
      long t = Instant.now().getEpochSecond();
      options.setId(t + "");
    }

    Pipeline pipeline = Pipeline.create(options);

    /**
     * **********************************************************************************************
     * INPUT
     * **********************************************************************************************
     */
    String[] inputUrl = options.getInputUrl().split(":");
    PCollection<byte[]> data =
      pipeline.apply("INPUT",
        new InputFromMqtt("tcp://" + inputUrl[0] + ":" + inputUrl[1], inputUrl[2]));

    /**
     * **********************************************************************************************
     * CLEAN UP
     * **********************************************************************************************
     */
    PCollection<TagData> cleanData =
      data.apply("CLEAN UP",
        new CleanUp(options.getEnv(), options.getHeader()));

    /**
     * **********************************************************************************************
     * ARCHIVE
     * **********************************************************************************************
     */
    String[] archiveUser = options.getArchiveUrl().split("@")[0].split(":");
    String[] archiveUrl = options.getArchiveUrl().split("@")[1].split(":");
    cleanData.apply("ARCHIVE",
      new ArchiveToMongoDB(
        "mongodb://" + archiveUser[0] + ":" + archiveUser[1] + "@" + archiveUrl[0] + ":" + archiveUrl[1],
        archiveUrl[2] + "-" + options.getId(),
        archiveUrl[3]));

    /**
     * **********************************************************************************************
     * LOG
     * **********************************************************************************************
     */
    if (options.getIsLogging()) {
      cleanData.apply("LOG <TagData>",
        new Print<TagData>());
    }

    if (options.getType().equals("new")) {
      PCollectionList<Result> resultDataList = cleanData.apply("DETECT EVENT", new NewEventDetector());
      for (PCollection<Result> p: resultDataList.getAll()) {
        String[] outputUser = options.getOutputUrl().split("@")[0].split(":");
        String[] outputUrl = options.getOutputUrl().split("@")[1].split(":");
        p.apply("OUTPUT",
          new OutputToMongoDB(
            "mongodb://" + outputUser[0] + ":" + outputUser[1] + "@" + outputUrl[0] + ":" + outputUrl[1],
            outputUrl[2] + "-" + options.getId(),
            outputUrl[3]));
      }
    } else {
      /**
       * **********************************************************************************************
       * DETECT EVENT
       * **********************************************************************************************
       */
      EventDetector detector = new EventDetector();
      if (options.getType().equals("1")) {
        detector = new EventDetectorPerLogic();
      } else if (options.getType().equals("2")) {
        detector = new EventDetectorPerGroup();
      } else if (options.getType().equals("cmd")) {
        detector = new CMDDetector();
      }
      PCollection<Result> resultData =
        cleanData.apply("DETECT EVENT", detector);

      /**
       * **********************************************************************************************
       * LOG
       * **********************************************************************************************
       */
      if (options.getIsLogging()) {
        resultData.apply("LOG <Result>",
          new Print<Result>());
      }

      /**
       * **********************************************************************************************
       * OUTPUT
       * **********************************************************************************************
       */
      String[] outputUser = options.getOutputUrl().split("@")[0].split(":");
      String[] outputUrl = options.getOutputUrl().split("@")[1].split(":");
      resultData.apply("OUTPUT",
        new OutputToMongoDB(
          "mongodb://" + outputUser[0] + ":" + outputUser[1] + "@" + outputUrl[0] + ":" + outputUrl[1],
          outputUrl[2] + "-" + options.getId(),
          outputUrl[3]));
    }

    pipeline.run().waitUntilFinish();
  }
}
