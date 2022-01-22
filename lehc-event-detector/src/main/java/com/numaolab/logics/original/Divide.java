package com.numaolab.logics.original;

import java.util.Map;

import com.numaolab.logics.interfaces.Logic;
import com.numaolab.schemas.SortedTagData;
import com.numaolab.schemas.TagData;

public class Divide implements Logic {
  @Override
  public Boolean detect(SortedTagData d) {
    if (d.getPrevTags().size() >= d.getK() && d.getPrevItagKeys().size() >= d.getKd() && d.getCurrTags().size() >= d.getK() && d.getCurrItagKeys().size() >= d.getKd()) {
      // rssiの平均値を計算
      float prevNITagsRssiAve = 0;
      int prevNISize = 0;
      for(Map.Entry<String, TagData> entry : d.getPrevTags().entrySet()){
        if (!d.getPrevItagKeys().contains(entry.getKey())) {
          prevNITagsRssiAve += Float.parseFloat(entry.getValue().getRssi());
          prevNISize += 1;
        }
      }
      prevNITagsRssiAve /= prevNISize;
      float prevITagsRssiAve = 0;
      int prevISize = 0;
      for(Map.Entry<String, TagData> entry : d.getPrevTags().entrySet()){
        if (d.getPrevItagKeys().contains(entry.getKey())) {
          prevITagsRssiAve += Float.parseFloat(entry.getValue().getRssi());
          prevISize += 1;
        }
      }
      prevITagsRssiAve /= prevISize;
      float currNITagsRssiAve = 0;
      int currNISize = 0;
      for(Map.Entry<String, TagData> entry : d.getCurrTags().entrySet()){
        if (!d.getCurrItagKeys().contains(entry.getKey())) {
          currNITagsRssiAve += Float.parseFloat(entry.getValue().getRssi());
          currNISize += 1;
        }
      }
      currNITagsRssiAve /= currNISize;
      float currITagsRssiAve = 0;
      int currISize = 0;
      for(Map.Entry<String, TagData> entry : d.getCurrTags().entrySet()){
        if (d.getPrevItagKeys().contains(entry.getKey())) {
          currITagsRssiAve += Float.parseFloat(entry.getValue().getRssi());
          currISize += 1;
        }
      }
      currITagsRssiAve /= currISize;
      // 差の絶対値を計算
      float prevDiff = Math.abs(prevITagsRssiAve - prevNITagsRssiAve);
      float currDiff = Math.abs(currITagsRssiAve - currNITagsRssiAve);
      // 閾値を取得
      int threshold = Integer.parseInt(d.getOther(), 2);
      // 閾値と比較
      if (prevDiff < threshold && currDiff >= threshold) {
        return true;
      }
    }
    return false;
  }
}
