package com.numaolab.mkd;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numaolab.enums.Logic;
import com.numaolab.json.SortedTagJson;
import com.numaolab.schemas.SortedTagData;
import com.numaolab.schemas.TagData;

public class SortedTagDataMkd extends SortedTagData {
  final Logic logic;
  final int k;
  final int kd;
  final String other;
  final Map<String, TagData> prevTags;
  final HashSet<String> prevItagKeys;
  final Map<String, TagData> currTags;
  final HashSet<String> currItagKeys;

  SortedTagDataMkd(Logic logic, int k, int kd, String other, Map<String, TagData> prevTags, Map<String, TagData> currTags) {
    this.logic = logic;
    this.k = k;
    this.kd = kd;
    this.other = other;
    this.prevTags = prevTags;
    this.currTags = currTags;
    this.prevItagKeys = prevTags.entrySet().stream().parallel().filter(e -> e.getValue().getMbit().equals("1")).map(e -> e.getKey()).collect(Collectors.toCollection(HashSet::new));
    this.currItagKeys = currTags.entrySet().stream().parallel().filter(e -> e.getValue().getMbit().equals("1")).map(e -> e.getKey()).collect(Collectors.toCollection(HashSet::new));
  }

  public static SortedTagDataMkd fromJson(SortedTagJson d) {
    return new SortedTagDataMkd(
      Logic.valueOf(d.logic),
      d.k,
      d.kd,
      d.other,
      new HashMap<String, TagData>(){{
        d.prevTags.forEach(t -> put(t.igs, new TagDataMkd(t.mbit ? "1" : "0", t.rssi.toString(), t.phase.toString())));
      }},
      new HashMap<String, TagData>(){{
        d.currTags.forEach(t -> put(t.igs, new TagDataMkd(t.mbit ? "1" : "0", t.rssi.toString(), t.phase.toString())));
      }}
    );
  }

  @Override
  public Logic getLogic() {
    return this.logic;
  }

  @Override
  public int getK() {
    return this.k;
  }

  @Override
  public int getKd() {
    return this.kd;
  }

  @Override
  public String getGid() {
    return null;
  }

  @Override
  public String getOther() {
    return this.other;
  }

  @Override
  public Map<String, TagData> getPrevTags() {
    return this.prevTags;
  }

  @Override
  public HashSet<String> getPrevItagKeys() {
    return this.prevItagKeys;
  }

  @Override
  public Map<String, TagData> getCurrTags() {
    return this.currTags;
  }

  @Override
  public HashSet<String> getCurrItagKeys() {
    return this.currItagKeys;
  }
}
