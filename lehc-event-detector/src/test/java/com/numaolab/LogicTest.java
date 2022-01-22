package com.numaolab;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numaolab.json.TestDataJson;
import com.numaolab.logics.Abridged;
import com.numaolab.mkd.SortedTagDataMkd;
import com.numaolab.schemas.SortedTagData;

import org.junit.Test;

public class LogicTest {

  Boolean eventDetect(SortedTagData d) {
    switch (d.getLogic()) {
      case DROP: return Abridged.drop.detect(d);
      case EMERGE: return Abridged.emerge.detect(d);
      case CROSS: return Abridged.cross.detect(d);
      case DIVIDE: return Abridged.divide.detect(d);
      case MERGE: return Abridged.merge.detect(d);
      default: return null;
    }
  }

  @Test
  public void test() {
    Path path = Paths.get("./src/test/java/com/numaolab/data");
    try (Stream<Path> stream = Files.find(path, 3, (p, b) -> p.toFile().getName().matches(".*.json"))) {
      stream.forEach(p -> {
        try {
          String json = new String(Files.readAllBytes(p));
          System.out.println(json);
          ObjectMapper mapper = new ObjectMapper();
          TestDataJson td = mapper.readValue(json, TestDataJson.class);
          SortedTagData d = SortedTagDataMkd.fromJson(td.data);
          assertEquals(td.title, td.answer, eventDetect(d));
        } catch (IOException e) {
          System.err.println(e);
        }
      });
    } catch (IOException e) {
      System.err.println(e);
      System.exit(1);
    }
  }
}
