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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class RpcFieldSpecTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testBasicField() {
    output
        .expects("// comment\nrpc Test (Foo) returns (Bar);\n")
        .produce(
            () ->
                RpcFieldSpec.builder("Test")
                    .setFieldComment("comment")
                    .setRequestMessageName("Foo")
                    .setResponseMessageName("Bar")
                    .build());
  }

  @Test
  public void testStreamFields() {
    output
        .expects("// comment\nrpc Test (stream Foo) returns (stream Bar);\n")
        .produce(
            () ->
                RpcFieldSpec.builder("Test")
                    .setFieldComment("comment")
                    .setRequestMessageName("Foo", true)
                    .setResponseMessageName("Bar", true)
                    .build());
  }

  @Test
  public void testRequestMessageMustBeSet() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("request message must be set");
    RpcFieldSpec.builder("Test").build();
  }

  @Test
  public void testResponseMessageMustBeSet() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("response message must be set");
    RpcFieldSpec.builder("Test").setRequestMessageName("test").build();
  }

  @Test
  public void testFieldOptions() {
    output
        .expects(
            "// comment\nrpc Test (Foo) returns (Bar) {\n  option (foo) = \"bar\";\n  option (baz) = true;\n}\n")
        .produce(
            () ->
                RpcFieldSpec.builder("Test")
                    .setFieldComment("comment")
                    .setRequestMessageName("Foo")
                    .setResponseMessageName("Bar")
                    .addFieldOptions(
                        OptionSpec.builder(OptionType.METHOD, "foo")
                            .setValue(FieldType.STRING, "bar"),
                        OptionSpec.builder(OptionType.METHOD, "baz").setValue(FieldType.BOOL, true))
                    .build());
  }
}
