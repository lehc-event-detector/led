package com.numaolab.logics.original;

import java.util.HashSet;

import com.numaolab.logics.interfaces.Logic;
import com.numaolab.schemas.SortedTagData;

public class Drop implements Logic {
  @Override
  public Boolean detect(SortedTagData d) {
    if (d.getPrevTags().size() >= d.getK() && d.getPrevItagKeys().size() >= d.getKd()) {
      HashSet<String> tmp = new HashSet<>(d.getPrevItagKeys());
      tmp.retainAll(d.getCurrItagKeys());
      if (tmp.size() == 0 && d.getCurrTags().size() <= d.getK()/2) {
        return true;
      }
    }
    return false;
  }
}
