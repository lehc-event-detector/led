package com.numaolab;

import org.junit.Test;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

public class LettuceTest {
  @Test
  public void run() {
    RedisClient client = RedisClient.create("redis://localhost:6379");
    StatefulRedisConnection<String, String> connection = client.connect();
    RedisAsyncCommands<String, String> commands = connection.async();

    commands.set("key", "Hello World!");

    connection.close();
    client.shutdown();
  }
}
