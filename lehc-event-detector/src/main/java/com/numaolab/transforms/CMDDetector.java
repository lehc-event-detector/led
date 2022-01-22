package com.numaolab.transforms;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import org.joda.time.Instant;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.numaolab.enums.Logic;
import com.numaolab.lib.scio.DoFnWithResource;
import com.numaolab.lib.scio.JavaAsyncDoFn;
import com.numaolab.logics.Cmd;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numaolab.Config;
import com.numaolab.schemas.Result;
import com.numaolab.schemas.TagData;

public class CMDDetector extends EventDetector {

  public static class Tags {
    public final Logic logic;
    public final int k;
    public final int kd;
    public final String gid;
    public final String other;
    public final List<TagData> niTags = new ArrayList<>();
    public final List<TagData> yiTags = new ArrayList<>();

    Tags(Collection<TagData> tags) {
      TagData anyTag = tags.stream().findAny().get();
      this.logic = Config.getLogic(anyTag.getLogic(), anyTag.getEri());
      this.k = Integer.parseInt(anyTag.getK(), 2);
      this.kd = Integer.parseInt(anyTag.getKd(), 2);
      this.gid = anyTag.getGid();
      this.other = anyTag.getOther();
      // 最新のものだけにフィルターする
      HashMap<String, Instant> cache = new HashMap<>();
      for (TagData t: tags) {
        String igs = t.getIgs();
        Instant time = Instant.ofEpochMilli(Long.parseLong(t.getTime())/1000);
        Boolean mbit = t.getMbit().equals("1");
        if (!cache.containsKey(igs) || time.isAfter(cache.get(igs))) {
          if (mbit) {
            this.yiTags.add(t);
          } else {
            this.niTags.add(t);
          }
          cache.put(igs, time);
        }
      }
    }

    public static Tags fromJson(String json) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        TagData[] tags = mapper.readValue(json, TagData[].class);
        return new Tags(Arrays.asList(tags));
      } catch (IOException e) {
        System.err.println(e);
        return new Tags(new ArrayList<>());
      }
    }

    public String toJson() {
      try {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Stream.concat(this.niTags.stream(), this.yiTags.stream()).collect(Collectors.toList()));
        return json;
      } catch (IOException e) {
        System.err.println(e);
        return "";
      }
    }
  }

  protected static class DetectCMD extends DoFn<KV<String, Iterable<TagData>>, Result> {

    protected static final Jedis jedis = new Jedis("redis", 6379);

    @ProcessElement
    public void process(ProcessContext ctx) {

      // System.out.println("HIT: " + ctx.timestamp());

      Tags curr = new Tags((Collection<TagData>) ctx.element().getValue());

      // 非重要タグと重要タグがそれぞれ1枚以上必要
      if (curr.niTags.size() < 1 || curr.yiTags.size() < 1) {
        return;
      }

      // キャッシュがあれば検出実行
      String cache = jedis.get(curr.gid);
      if (cache != null) {
        Tags prev = Tags.fromJson(cache);
        if (
          (curr.logic == Logic.CROSS && Cmd.detectCross(prev, curr)) ||
          (curr.logic == Logic.MERGE && Cmd.detectMerge(prev, curr)) ||
          (curr.logic == Logic.DIVIDE && Cmd.detectDivide(prev, curr))
        ) {
          ctx.output(Result.create(curr.gid, curr.logic, ctx.timestamp()));
        }
      }

      // キャッシュを更新
      jedis.set(curr.gid, curr.toJson());
    }
  }

  protected static class DetectCMD2 extends DoFn<KV<String, Iterable<TagData>>, Result> {
    @ProcessElement
    public void process(ProcessContext ctx) {

      System.out.println("HIT: " + ctx.timestamp());

      Jedis jedis = new Jedis("redis", 6379);

      Tags curr = new Tags((Collection<TagData>) ctx.element().getValue());

      // 非重要タグと重要タグがそれぞれ1枚以上必要
      if (curr.niTags.size() < 1 || curr.yiTags.size() < 1) {
        jedis.close();
        return;
      }

      // キャッシュがあれば検出実行
      String cache = jedis.get(curr.gid);
      if (cache != null) {
        Tags prev = Tags.fromJson(cache);
        if (
          (curr.logic == Logic.CROSS && Cmd.detectCross(prev, curr)) ||
          (curr.logic == Logic.MERGE && Cmd.detectMerge(prev, curr)) ||
          (curr.logic == Logic.DIVIDE && Cmd.detectDivide(prev, curr))
        ) {
          ctx.output(Result.create(curr.gid, curr.logic, ctx.timestamp()));
        }
      }

      // キャッシュを更新
      jedis.set(curr.gid, curr.toJson());

      jedis.close();
    }
  }

  protected static class DetectCMDLettuce extends DoFn<KV<String, Iterable<TagData>>, Result> {

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisAsyncCommands<String, String> commands;

    @Setup
    public void setup() {
      System.out.println("setUp");

      System.out.println("createResource");
      this.redisClient = RedisClient.create("redis://redis:6379");
      this.connection = redisClient.connect();
      this.commands = connection.async();
    }

    @StartBundle
    public void startBundle(DoFn<KV<String, Iterable<TagData>>, Result>.StartBundleContext startBundleContext) {
        System.out.println("startBundle");
    }

    @ProcessElement
    public void process(ProcessContext ctx) {

      System.out.println("HIT: " + ctx.timestamp());

      Tags curr = new Tags((Collection<TagData>) ctx.element().getValue());

      // 非重要タグと重要タグがそれぞれ1枚以上必要
      if (curr.niTags.size() < 1 || curr.yiTags.size() < 1) {
        return;
      }
      try {
        // キャッシュがあれば検出実行
        RedisFuture<String> future = this.commands.get(curr.gid);
        String cache = future.get();
        if (cache != null) {
          Tags prev = Tags.fromJson(cache);
          if (
            (curr.logic == Logic.CROSS && Cmd.detectCross(prev, curr)) ||
            (curr.logic == Logic.MERGE && Cmd.detectMerge(prev, curr)) ||
            (curr.logic == Logic.DIVIDE && Cmd.detectDivide(prev, curr))
          ) {
            ctx.output(Result.create(curr.gid, curr.logic, ctx.timestamp()));
          }
        }

        // キャッシュを更新
        this.commands.set(curr.gid, curr.toJson());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }

    @FinishBundle
    public void finishBundle(DoFn<KV<String, Iterable<TagData>>, Result>.FinishBundleContext finishBundleContext) {
        System.out.println("finishBundleStart");
        System.out.println("finishBundleEnd");
    }

    @Teardown
    public void tearDown(){
        System.out.println("tearDown");

        if (this.connection != null) {
            this.connection.close();
        }
        if (this.redisClient != null) {
            this.redisClient.shutdown();
        }
    }
  }

  protected static class AsyncDetectCMD extends JavaAsyncDoFn<KV<String, Iterable<TagData>>, Result, RedisAsyncCommands<String, String>> {
    @Override
    public CompletableFuture<Result> processElement(KV<String, Iterable<TagData>> input) {
      RedisAsyncCommands<String, String> commands = getResource();

      System.out.println("HIT: ");

      Tags curr = new Tags((Collection<TagData>) input.getValue());

      final RedisFuture<String> future = commands.get(curr.gid);
      return future.thenApply((new Function<String, Result>() {
        @Override
        public Result apply(String cache) {
          Result res = Result.create(curr.gid, curr.logic, Instant.now());
          // 非重要タグと重要タグがそれぞれ1枚以上必要
          if (curr.niTags.size() < 1 || curr.yiTags.size() < 1) {

          } else {
            if (cache != null) {
              Tags prev = Tags.fromJson(cache);
              if (
                (curr.logic == Logic.CROSS && Cmd.detectCross(prev, curr)) ||
                (curr.logic == Logic.MERGE && Cmd.detectMerge(prev, curr)) ||
                (curr.logic == Logic.DIVIDE && Cmd.detectDivide(prev, curr))
              ) {
                res = Result.create(curr.gid, curr.logic, Instant.now());
              }
            }
            // キャッシュを更新
            commands.set(curr.gid, curr.toJson());
          }

          return res;
        }
      })).toCompletableFuture();
    }

    @Override
    public DoFnWithResource.ResourceType getResourceType() {
        return DoFnWithResource.ResourceType.PER_CLONE;
    }

    @Override
    public RedisAsyncCommands<String, String> createResource() {
      RedisClient redisClient = RedisClient.create("redis://redis:6379");
      StatefulRedisConnection<String, String> connection = redisClient.connect();
      RedisAsyncCommands<String, String> commands = connection.async();
      return commands;
    }
  }

  @Override
  public PCollection<Result> expand(PCollection<TagData> tagDataRows) {

    /**
     * **********************************************************************************************
     * Fixed Window
     * **********************************************************************************************
     */
    PCollection<TagData> windowingData =
        tagDataRows.apply(
          "Windowing",
          Window.<TagData>into(
            FixedWindows.of(windowEvery)
          ).triggering(Repeatedly.forever(AfterWatermark.pastEndOfWindow())).withAllowedLateness(Duration.ZERO).discardingFiredPanes());
          // .withAllowedLateness(Duration.ZERO).discardingFiredPanes()

    /**
     * **********************************************************************************************
     * Map <GID, TagData>
     * **********************************************************************************************
     */
    PCollection<KV<String, TagData>> kvData =
        windowingData.apply("Map", ParDo.of(new MapGidKey()));


    /**
     * **********************************************************************************************
     * GroupByGID <GID, Iterable<TagData>>
     * **********************************************************************************************
     */
    PCollection<KV<String, Iterable<TagData>>> groupData =
        kvData.apply("GroupByGID", GroupByKey.create());

    /**
     * **********************************************************************************************
     * Detect Logic per Group
     * **********************************************************************************************
     */
    PCollection<Result> result =
        groupData.apply("Detect Logic per Group", ParDo.of(new DetectCMD2()));

    return result;
  }
}
