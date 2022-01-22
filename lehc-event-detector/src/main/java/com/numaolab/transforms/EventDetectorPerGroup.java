package com.numaolab.transforms;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.numaolab.schemas.Result;
import com.numaolab.schemas.TagData;

public class EventDetectorPerGroup extends EventDetector {
  @Override
  public PCollection<Result> expand(PCollection<TagData> tagDataRows) {
    /**
     * **********************************************************************************************
     * Map <GID, TagData>
     * **********************************************************************************************
     */
    PCollection<KV<String, TagData>> kvData =
        tagDataRows.apply("Map", ParDo.of(new MapGidKey()));

    /**
     * **********************************************************************************************
     * Create Pipeline per Logic
     * **********************************************************************************************
     */
    PCollectionList<Result> cs = PCollectionList.of(IntStream.range(0, 100).mapToObj(i -> {
        /**
         * **********************************************************************************************
         * Filter by GroupID
         * **********************************************************************************************
         */
        PCollection<KV<String, TagData>> filteredKvData = kvData.apply(
          ParDo.of(
            new DoFn<KV<String, TagData>, KV<String, TagData>>() {
              @ProcessElement
              public void processElement(ProcessContext c) {
                if (i == Integer.parseInt(c.element().getValue().getGid(), 2)) {
                  c.output(c.element());
                }
              }
            }
          )
        );

        /**
         * **********************************************************************************************
         * Windowing
         * **********************************************************************************************
         */
        PCollection<KV<String, TagData>> windowingData =
            filteredKvData.apply(
              "Windowing",
              Window.<KV<String, TagData>>into(
                SlidingWindows.of(windowSize).every(windowEvery)));

        /**
         * **********************************************************************************************
         * GroupByGID <GID, Iterable<TagData>>
         * **********************************************************************************************
         */
        PCollection<KV<String, Iterable<TagData>>> groupData =
            windowingData.apply("GroupByGID", GroupByKey.create());

        /**
         * **********************************************************************************************
         * Detect Logic per Group
         * **********************************************************************************************
         */
        PCollection<Result> result =
            groupData.apply("Detect Logic per Group", ParDo.of(new DetectLogic()));

        return result;
      }).collect(Collectors.toList()));

    /**
     * **********************************************************************************************
     * Create Pipeline per Logic
     * **********************************************************************************************
     */
    return cs.apply(Flatten.<Result>pCollections());
  }
}
