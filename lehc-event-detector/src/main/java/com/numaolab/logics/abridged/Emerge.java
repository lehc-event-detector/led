package com.numaolab.logics.abridged;

import com.numaolab.logics.interfaces.Logic;
import com.numaolab.schemas.SortedTagData;

public class Emerge implements Logic {
  @Override
  public Boolean detect(SortedTagData d) {
    return d.getPrevTags().size() < d.getK() && d.getCurrTags().size() == d.getK();
  }
}
