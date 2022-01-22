package com.numaolab.logics.abridged;

import com.numaolab.logics.interfaces.Logic;
import com.numaolab.schemas.SortedTagData;

public class Drop implements Logic {
  @Override
  public Boolean detect(SortedTagData d) {
    return d.getPrevTags().size() > 0 && d.getCurrTags().size() == 0;
  }
}
