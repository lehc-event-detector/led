package com.numaolab.logics.original;

import java.util.HashSet;

import com.numaolab.logics.interfaces.Logic;
import com.numaolab.schemas.SortedTagData;

public class Emerge implements Logic {
  @Override
  public Boolean detect(SortedTagData d) {
    if (d.getCurrTags().size() >= d.getK() && d.getCurrItagKeys().size() >= d.getKd()) {
      HashSet<String> tmp = new HashSet<>(d.getCurrItagKeys());
      tmp.retainAll(d.getPrevItagKeys());
      if (tmp.size() == 0 && d.getPrevTags().size() <= d.getK()/2) {
        return true;
      }
    }
    return false;
  }
}
