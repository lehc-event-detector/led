package com.numaolab.logics.original;

import java.util.HashSet;

import com.numaolab.logics.interfaces.Logic;
import com.numaolab.schemas.SortedTagData;

public class Cross implements Logic {
  @Override
  public Boolean detect(SortedTagData d) {
    if (d.getPrevTags().size() + d.getCurrTags().size() >= d.getK() && d.getPrevItagKeys().size() + d.getCurrItagKeys().size() >= d.getKd()) {
      HashSet<String> tmp = new HashSet<>(d.getPrevItagKeys());
      tmp.retainAll(d.getCurrItagKeys());
      if (d.getCurrItagKeys().size() >= 0 && tmp.size() == 0) {
        return true;
      }
    }
    return false;
  }
}
