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

import redis.clients.jedis.Jedis;

public class SampleTest {

  @Test
  public void test() {
    Jedis jedis = new Jedis("localhost", 6379);

    // Redisへの登録
    jedis.set("key", "100");
    // Redisから値を取得
    // String c = jedis.get("a");
    // System.out.println(Integer.parseInt(c == null ? c : "0"));
    // Redisから削除
    // jedis.del("key");

    jedis.close();
  }

  @Test
  public void getCounter() {
    Jedis jedis = new Jedis("localhost", 6379);

    // Redisから値を取得
    System.out.println("key = " + Integer.parseInt(jedis.get("counter")));

    jedis.close();
  }
}
