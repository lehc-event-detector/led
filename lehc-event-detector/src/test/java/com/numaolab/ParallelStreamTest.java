package com.numaolab;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numaolab.json.TestDataJson;
import com.numaolab.logics.Abridged;
import com.numaolab.mkd.SortedTagDataMkd;
import com.numaolab.schemas.SortedTagData;

import org.junit.Test;

import redis.clients.jedis.Jedis;

public class ParallelStreamTest {

  @Test
  public void test() {
    List<String> tmp = new ArrayList<>();
    IntStream.range(0, 1_000_000).parallel().forEach(i -> {
      tmp.add(i + "");
    });
    System.out.println(tmp);

    // HashSet<String> cache = new HashSet<>();
    //   tags.parallelStream().forEach(t -> {
    //     if (!cache.contains(t.getIgs())) {
    //       if (t.getMbit().equals("1")) {
    //         this.yiTags.add(t);
    //       } else {
    //         this.niTags.add(t);
    //       }
    //       cache.add(t.getIgs());
    //     }
    //   });
  }
}
