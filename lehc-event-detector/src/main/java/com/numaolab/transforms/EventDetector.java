package com.numaolab.transforms;

import org.apache.beam.sdk.coders.VarIntCoder;
import org.apache.beam.sdk.state.StateSpec;
import org.apache.beam.sdk.state.StateSpecs;
import org.apache.beam.sdk.state.ValueState;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import org.joda.time.Instant;

import redis.clients.jedis.Jedis;

import java.util.Collection;

import com.numaolab.enums.Logic;
import com.numaolab.logics.Abridged;
import com.numaolab.logics.Original;
import com.numaolab.Config;
import com.numaolab.schemas.Result;
import com.numaolab.schemas.TagData;
import com.numaolab.schemas.SortedTagData;

public class EventDetector extends PTransform<PCollection<TagData>, PCollection<Result>> {
  protected static final Duration windowSize = Config.windowSize;
  protected static final Duration windowEvery = Config.windowEvery;
  protected static final Duration timeSkew = Config.timeSkew;

  protected static class MapGidKey extends DoFn<TagData, KV<String, TagData>> {
    @ProcessElement
    public void process(@Element TagData t, OutputReceiver<KV<String, TagData>> out, IntervalWindow w) {
      // System.out.println("{{ " + w.start().toString() + ", " + w.end().toString() + "}}");
      out.output(KV.of(t.getGid(), t));
    }
  }

  protected static class DetectLogic extends DoFn<KV<String, Iterable<TagData>>, Result> {

    @ProcessElement
    public void process(@Element KV<String, Iterable<TagData>> data, @Timestamp Instant timestamp, OutputReceiver<Result> out, IntervalWindow w) {

      SortedTagData d = SortedTagData.create((Collection<TagData>) data.getValue(), w, timeSkew, windowEvery);
      if (d.getLogic() == Logic.DROP && Abridged.drop.detect(d)) {
        out.output(Result.create(d.getGid(), Logic.DROP, timestamp));
      } else if (d.getLogic() == Logic.CROSS && Abridged.cross.detect(d)) {
        out.output(Result.create(d.getGid(), Logic.CROSS, timestamp));
      } else if (d.getLogic() == Logic.EMERGE && Abridged.emerge.detect(d)) {
        out.output(Result.create(d.getGid(), Logic.EMERGE, timestamp));
      } else if (d.getLogic() == Logic.DIVIDE && Abridged.divide.detect(d)) {
        out.output(Result.create(d.getGid(), Logic.DIVIDE, timestamp));
      } else if (d.getLogic() == Logic.MERGE && Abridged.merge.detect(d)) {
        out.output(Result.create(d.getGid(), Logic.MERGE, timestamp));
      }
    }
  }

  protected static class DetectDE extends DoFn<KV<String, Iterable<TagData>>, Result> {

    @ProcessElement
    public void process(@Element KV<String, Iterable<TagData>> data, @Timestamp Instant timestamp, OutputReceiver<Result> out, IntervalWindow w) {

      SortedTagData d = SortedTagData.create((Collection<TagData>) data.getValue(), w, timeSkew, windowEvery);
      if (d.getLogic() == Logic.DROP && Abridged.drop.detect(d)) {
        out.output(Result.create(d.getGid(), Logic.DROP, timestamp));
      } else if (d.getLogic() == Logic.EMERGE && Abridged.emerge.detect(d)) {
        out.output(Result.create(d.getGid(), Logic.EMERGE, timestamp));
      }
    }
  }

  @Override
  public PCollection<Result> expand(PCollection<TagData> tagDataRows) {
    /**
     * **********************************************************************************************
     * Windowing
     * **********************************************************************************************
     */
    PCollection<TagData> windowingData =
      tagDataRows.apply(
          "Windowing",
          Window.<TagData>into(
            SlidingWindows.of(windowSize).every(windowEvery)));

    /**
     * **********************************************************************************************
     * Map <GID, TagData>
     * **********************************************************************************************
     */
    PCollection<KV<String, TagData>> kvData =
        windowingData.apply("Map <GID, TagData>", ParDo.of(new MapGidKey()));

    /**
     * **********************************************************************************************
     * GroupByGID <GID, Iterable<TagData>>
     * **********************************************************************************************
     */
    PCollection<KV<String, Iterable<TagData>>> groupData =
        kvData.apply("GroupByGID", GroupByKey.create());

    /**
     * **********************************************************************************************
     * Detect Logic per Group
     * **********************************************************************************************
     */
    PCollection<Result> result =
        groupData.apply("Detect Logic per Group", ParDo.of(new DetectDE()));

    return result;
  }
}