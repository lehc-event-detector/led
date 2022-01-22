package com.numaolab.schemas;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.auto.value.AutoValue;
import com.numaolab.Config;
import com.numaolab.enums.Logic;

import org.apache.beam.sdk.schemas.AutoValueSchema;
import org.apache.beam.sdk.schemas.annotations.DefaultSchema;
import org.apache.beam.sdk.schemas.annotations.SchemaCreate;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.joda.time.Duration;
import org.joda.time.Instant;

@DefaultSchema(AutoValueSchema.class)
@AutoValue
public abstract class SortedTagData {
  public abstract Logic getLogic();
  public abstract int getK();
  public abstract int getKd();
  public abstract String getGid();
  public abstract String getOther();
  public abstract Map<String, TagData> getPrevTags();
  public abstract HashSet<String> getPrevItagKeys();
  public abstract Map<String, TagData> getCurrTags();
  public abstract HashSet<String> getCurrItagKeys();

  @SchemaCreate
  public static SortedTagData create(Collection<TagData> tags, IntervalWindow w, Duration timeSkew, Duration windowEvery) {
    Logic logic;
    int k, kd;
    String gid, other;
    Map<String, TagData> prevTags = new HashMap<>(), currTags = new HashMap<>();
    HashSet<String> prevItagKeys = new HashSet<>(), currItagKeys = new HashSet<>();
    // 適当に1つとる
    TagData anyTag = tags.stream().findFirst().get();
    // ロジック
    logic = Config.getLogic(anyTag.getLogic(), anyTag.getEri());
    // kとkd
    k = Integer.parseInt(anyTag.getK(), 2);
    kd = Integer.parseInt(anyTag.getKd(), 2);
    // Gid
    gid = anyTag.getGid();
    // Other
    other = anyTag.getOther();
    // prevとcurr
    Instant start = w.start();
    Instant middle = w.start().plus(windowEvery);
    Instant end = w.end();
    BiConsumer<Map<String, TagData>, TagData> putIn = (tagMap, tag) -> {
      String igs = tag.getIgs();
      if (!tagMap.containsKey(igs)) {
        tagMap.put(igs, tag);
      } else {
        Instant timestamp = Instant.ofEpochMilli(Long.parseLong(tag.getTime())/1000);
        Instant fTimestamp = Instant.ofEpochMilli(Long.parseLong(tagMap.get(igs).getTime())/1000);
        if (timestamp.isAfter(fTimestamp)) {
          tagMap.put(igs, tag);
        }
      }
    };
    for (TagData t: tags) {
      Instant timestamp = Instant.ofEpochMilli(Long.parseLong(t.getTime())/1000).plus(timeSkew);
      if (timestamp.compareTo(start) >= 0 && timestamp.isBefore(middle)) {
        putIn.accept(prevTags, t);
        if (t.getMbit().equals("1")) {
          prevItagKeys.add(t.getIgs());
        }
      } else if (timestamp.compareTo(middle) >= 0 && timestamp.isBefore(end)) {
        putIn.accept(currTags, t);
        if (t.getMbit().equals("1")) {
          currItagKeys.add(t.getIgs());
        }
      }
    }

    return new AutoValue_SortedTagData(logic, k, kd, gid, other, prevTags, prevItagKeys, currTags, currItagKeys);
  }
}