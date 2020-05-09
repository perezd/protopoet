/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package protopoet;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Simple utility rule that eliminates standard boilerplate for comparing the output of an emittable
 * to a string literal or convention-based testdata.
 */
public final class ExpectedOutput implements TestRule {

  static ExpectedOutput none() {
    return new ExpectedOutput();
  }

  private final StringBuilder out = new StringBuilder();
  private final ProtoWriter writer = new ProtoWriter(out);

  private String outputValue;
  private Supplier<Emittable> emittableProducer;
  private boolean expectTestData;

  private ExpectedOutput() {}

  public ExpectedOutput expects(String outputValue) {
    this.outputValue = outputValue;
    return this;
  }

  public ExpectedOutput expectsTestData() {
    this.expectTestData = true;
    return this;
  }

  public void produce(Supplier<Emittable> emittableProducer) {
    this.emittableProducer = emittableProducer;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        base.evaluate();
        if (emittableProducer != null) {
          emittableProducer.get().emit(writer);

          String realOutputValue;
          if (outputValue != null) {
            realOutputValue = outputValue;
          } else if (expectTestData) {
            realOutputValue = resourceAsString(testDataName(description));
          } else {
            throw new IllegalStateException("no output value configured");
          }
          assertThat(out.toString()).isEqualTo(realOutputValue);
        }
      }
    };
  }

  private String testDataName(Description description) {
    return String.format(
        "%s_%s", description.getTestClass().getSimpleName(), description.getMethodName());
  }

  private static String resourceAsString(String resName) throws IOException {
    URL res = Resources.getResource("testdata/" + resName + ".txt");
    return Resources.asCharSource(res, StandardCharsets.UTF_8).read();
  }
}
