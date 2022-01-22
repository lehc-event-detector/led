package com.numaolab;

import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Instant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

// import com.numaolab.enums.Logic;
import com.numaolab.schemas.Answer;
import com.numaolab.schemas.Result;
import com.numaolab.schemas.TagData;
import com.numaolab.transforms.EventDetector;
import com.numaolab.transforms.Print;

@RunWith(Parameterized.class)
public class LEHCEventDetectorTest {

  // private static class Verify implements SerializableFunction<Iterable<Result>, Void> {

  //   private final Iterable<Answer> ans;
  //   private final String fileName;

  //   private Verify(Iterable<Answer> ans, String fileName) {
  //     this.ans = ans;
  //     this.fileName = fileName;
  //   }

  //   @Override
  //   public Void apply(Iterable<Result> res) {
  //     for (Answer a: ans) {
  //       boolean isAnsSatisfied = false;
  //       for (Result r: res) {
  //         isAnsSatisfied = r.getGid().equals(a.getGid()) && r.getLogic().equals(a.getLogic()) && r.getCorrectedDetectedAt().compareTo(a.getStart()) >= 0 && r.getCorrectedDetectedAt().isBefore(a.getEnd());
  //         if (isAnsSatisfied) break;
  //       }
  //       assertTrue("FAILED: " + a + " is not satisfied. in " + this.fileName, isAnsSatisfied);
  //     }
  //     return null;
  //   }
  // }

  // @Rule
  // public final transient TestPipeline pipeline = TestPipeline.create();

  // @Parameters
  // public static Iterable<Object[]> data() {
  //   List<Object[]> testFiles = new ArrayList<>();
  //   try(Stream<Path> stream = Files.list(Paths.get("data"))) {
  //     stream.forEach(p -> {
  //       if (!Files.exists(Paths.get(p.toString() + "/ignore"))) {
  //         testFiles.add(new String[] {p.toString() + "/test", p.toString() + "/ans"});
  //       }
  //     });
  //   }catch(IOException e) {
  //     System.out.println(e);
  //   }
  //   return testFiles;
  // }

  // @Parameter(0)
  // public String testFilePath;

  // @Parameter(1)
  // public String ansFilePath;

  // @Test
  // public void run() throws IOException {
  //   // TestStream<String> createEvents = TestStream.create(StringUtf8Coder.of()).addElements(testData.get(0), testData.subList(1, testData.size()).toArray(new String[testData.size()-1])).advanceWatermarkToInfinity();
  //   // PCollection<String> jsonStrings = pipeline.apply(createEvents);
  //   PCollection<String> jsonStrings = pipeline.apply(TextIO.read().from(testFilePath));
  //   // jsonStrings.apply("a",new PrintResult<String>());
  //   PCollection<TagData> tagDataRows = jsonStrings.apply(new JsonStringToTagData());
  //   PCollection<Result> result = tagDataRows.apply(new EventDetector());
  //   PAssert.that(result).satisfies(new Verify(readAnsFIle(ansFilePath), ansFilePath));
  //   result.apply(new Print<Result>());
  //   pipeline.run();
  // }

  // private List<Answer> readAnsFIle(String filepath) throws IOException {
  //   try (Stream<String> stream = Files.lines(Paths.get(filepath))) {
  //     List<Answer> ansData = new ArrayList<>();
  //     stream.forEach(json -> ansData.add(Answer.fromJson(json)));
  //     return ansData;
  //   }
  // }
}

// https://www.tabnine.com/web/assistant/code/rs/5c65644e1095a50001493b56#L125
// https://kazurof.github.io/work/tryjunit4/tryjunit4More.html
// https://tools.knowledgewalls.com/jsontostring