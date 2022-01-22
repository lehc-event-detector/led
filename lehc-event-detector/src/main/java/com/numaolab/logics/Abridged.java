package com.numaolab.logics;

import com.numaolab.logics.interfaces.Logic;

public class Abridged {
  public static final Logic drop = new com.numaolab.logics.abridged.Drop();
  public static final Logic emerge = new com.numaolab.logics.abridged.Emerge();
  public static final Logic cross = new com.numaolab.logics.abridged.Cross();
  public static final Logic divide = new com.numaolab.logics.original.Divide();
  public static final Logic merge = new com.numaolab.logics.original.Merge();
}
