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

import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class ServiceSpecTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testWritingEmptyService() {
    output
        .expects("service TestService {}\n")
        .produce(() -> ServiceSpec.builder("TestService").build());
  }

  @Test
  public void testWritingServiceComment() {
    output
        .expects("// this is a test\nservice A {}\n")
        .produce(() -> ServiceSpec.builder("A").setServiceComment("this is a test").build());
  }

  @Test
  public void testWritingRpcFields() {
    output
        .expectsTestData()
        .produce(
            () ->
                ServiceSpec.builder("A")
                    .setServiceComment("comment")
                    .addRpcFields(
                        RpcFieldSpec.builder("A")
                            .setFieldComment("comment A")
                            .setRequestMessageName("Req")
                            .setResponseMessageName("Res"),
                        RpcFieldSpec.builder("B")
                            .setFieldComment("comment B")
                            .setRequestMessageName("Req", true)
                            .setResponseMessageName("Res"),
                        RpcFieldSpec.builder("C")
                            .setFieldComment("comment C")
                            .setRequestMessageName("Req")
                            .setResponseMessageName("Res", true),
                        RpcFieldSpec.builder("D")
                            .setFieldComment("comment D")
                            .setRequestMessageName("Req", true)
                            .setResponseMessageName("Res", true))
                    .build());
  }

  @Test
  public void testEnsureFieldUniqueness() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage("protopoet.UsageException: field name 'A' is not unique");
    ServiceSpec.builder("A")
        .addRpcFields(
            RpcFieldSpec.builder("A").setRequestMessageName("Foo").setResponseMessageName("Bar"))
        .addRpcFields(
            RpcFieldSpec.builder("A").setRequestMessageName("Foo").setResponseMessageName("Bar"))
        .build()
        .emit(ProtoWriter.dud());
  }

  @Test
  public void testWritingServiceOptions() {
    output
        .expectsTestData()
        .produce(
            () ->
                ServiceSpec.builder("A")
                    .setServiceComment("comment")
                    .addServiceOptions(
                        OptionSpec.builder(OptionType.SERVICE, "b")
                            .setOptionComment("comment")
                            .setValue(FieldType.DOUBLE, 0.2),
                        OptionSpec.builder(OptionType.SERVICE, "c")
                            .setOptionComment("comment")
                            .setValue(FieldType.FLOAT, 2f))
                    .build());
  }

  @Test
  public void testEnsureOptionTypeEnforced() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("option must be service type");
    ServiceSpec.builder("A").addServiceOptions(OptionSpec.builder(OptionType.MESSAGE, "b"));
  }
}
