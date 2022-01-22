package com.numaolab.transforms;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

public class Print<T> extends PTransform<PCollection<T>, PCollection<T>> {

  private static class PrintEach<T> extends DoFn<T, T> {
    @ProcessElement
    public void process(@Element T r, OutputReceiver<T> out) {
      System.out.println(r);
      out.output(r);
    }
  }

  @Override
  public PCollection<T> expand(PCollection<T> result) {
    return result.apply(ParDo.of(new PrintEach<T>()));
  }
}