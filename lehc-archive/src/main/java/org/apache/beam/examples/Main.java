package org.apache.beam.examples;

import java.util.*;

import org.joda.time.Instant;

public class Main {
  public static void main(String[] args) throws Exception {
    long unixTimestamp = Instant.now().getMillis();
    System.out.println(Instant.ofEpochMilli(unixTimestamp));
    System.out.println(Instant.ofEpochMilli(Long.parseLong("1637744723724933")/1000));
  }
}
