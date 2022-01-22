package com.numaolab.jacksonTest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numaolab.json.TestDataJson;
import com.numaolab.logics.Abridged;
import com.numaolab.mkd.SortedTagDataMkd;
import com.numaolab.schemas.SortedTagData;
import com.numaolab.schemas.TagData;

import org.junit.Test;

public class JacksonTest {

  // private static class TagJson {
  //   public String header;
  //   public String other;
  //   public String env;
  //   public String eri;
  //   public String logic;
  //   public String k;
  //   public String kd;
  //   public String gid;
  //   public String mbit;
  //   public String igs;
  //   public String rssi;
  //   public String phase;
  //   public String time;
  // }

  // private static class TagsJson {
  //   public List<TagJson> tags;
  // }

  @Test
  public void test() {
    try {
      System.out.println("START");
      Path path = Paths.get("./src/test/java/com/numaolab/jacksonTest/test.json");
      String json = new String(Files.readAllBytes(path));
      System.out.println(json);
      ObjectMapper mapper = new ObjectMapper();
      TagData[] t = mapper.readValue(json, TagData[].class);
      System.out.println(t.length);
    } catch (IOException e) {
      System.err.println(e);
    }
  }
}
