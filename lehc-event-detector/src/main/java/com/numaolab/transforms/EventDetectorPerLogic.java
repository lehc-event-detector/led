package com.numaolab.transforms;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.numaolab.Config;
import com.numaolab.schemas.Result;
import com.numaolab.schemas.TagData;

public class EventDetectorPerLogic extends EventDetector {
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
    PCollectionList<Result> cs = PCollectionList.of(Config.logicMap.values().stream().map(l -> {
        /**
         * **********************************************************************************************
         * Filter by Logic
         * **********************************************************************************************
         */
        PCollection<KV<String, TagData>> filteredKvData = kvData.apply(
          ParDo.of(
            new DoFn<KV<String, TagData>, KV<String, TagData>>() {
              @ProcessElement
              public void processElement(ProcessContext c) {
                if (l == Config.getLogic(c.element().getValue().getLogic(), c.element().getValue().getEri())) {
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
