package com.numaolab.mkd;

import com.numaolab.schemas.TagData;

public class TagDataMkd extends TagData {
  final String mbit;
  final String rssi;
  final String phase;

  TagDataMkd(String mbit, String rssi, String phase) {
    this.mbit = mbit;
    this.rssi = rssi;
    this.phase = phase;
  }

  @Override
  public String getHeader() {
    return null;
  }

  @Override
  public String getOther() {
    return null;
  }

  @Override
  public String getEnv() {
    return null;
  }

  @Override
  public String getEri() {
    return null;
  }

  @Override
  public String getLogic() {
    return null;
  }

  @Override
  public String getK() {
    return null;
  }

  @Override
  public String getKd() {
    return null;
  }

  @Override
  public String getGid() {
    return null;
  }

  @Override
  public String getMbit() {
    return this.mbit;
  }

  @Override
  public String getIgs() {
    return null;
  }

  @Override
  public String getRssi() {
    return this.rssi;
  }

  @Override
  public String getPhase() {
    return this.phase;
  }

  @Override
  public String getTime() {
    return null;
  }
}
