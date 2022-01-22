package com.numaolab.logics.abridged;

import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.numaolab.logics.interfaces.Logic;
import com.numaolab.schemas.SortedTagData;
import com.numaolab.schemas.TagData;

public class Cross implements Logic {

  private Double rssiAve(SortedTagData d, boolean isPrev, boolean isImportant) {
    return (isPrev ? d.getPrevTags() : d.getCurrTags())
            .entrySet()
            .parallelStream()
            .filter(
              ((Supplier<Predicate<Entry<String, TagData>>>) () -> {
                if (isPrev) {
                  if (isImportant) {
                    return e -> d.getPrevItagKeys().contains(e.getKey());
                  } else {
                    return e -> !d.getPrevItagKeys().contains(e.getKey());
                  }
                } else {
                  if (isImportant) {
                    return e -> d.getCurrItagKeys().contains(e.getKey());
                  } else {
                    return e -> !d.getCurrItagKeys().contains(e.getKey());
                  }
                }
              }).get()
            )
            .mapToDouble(e -> Float.parseFloat(e.getValue().getRssi()))
            .average()
            .orElse(-999);
  }

  @Override
  public Boolean detect(SortedTagData d) {
    double prevITagsRssiAve = rssiAve(d, true, true);
    double prevNITagsRssiAve = rssiAve(d, true, false);
    double currITagsRssiAve = rssiAve(d, false, true);
    double currNITagsRssiAve = rssiAve(d, false, false);
    if (prevITagsRssiAve < prevNITagsRssiAve) {
      if (currITagsRssiAve > currNITagsRssiAve) {
        return true;
      } else {
        return false;
      }
    } else {
      if (currITagsRssiAve < currNITagsRssiAve) {
        return true;
      } else {
        return false;
      }
    }
  }
}
