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

public final class MessageFieldSpecTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testBasicField() {
    output
        .expects("// comment\nsfixed32 test = 1;\n")
        .produce(
            () ->
                MessageFieldSpec.builder(FieldType.SFIXED32, "test", 1)
                    .setFieldComment("comment")
                    .build());
  }

  @Test
  public void testRepeatedField() {
    output
        .expects("// comment\nrepeated bool test = 1;\n")
        .produce(
            () ->
                MessageFieldSpec.builder(FieldType.BOOL, "test", 1)
                    .setFieldComment("comment")
                    .setRepeated(true)
                    .build());
  }

  @Test
  public void testCustomTypeNameFieldMessage() {
    output
        .expects("// comment\nrepeated Foo test = 1;\n")
        .produce(
            () ->
                MessageFieldSpec.builder(FieldType.MESSAGE, "test", 1)
                    .setFieldComment("comment")
                    .setRepeated(true)
                    .setCustomTypeName("Foo")
                    .build());
  }

  @Test
  public void testCustomTypeNameFieldEnum() {
    output
        .expects("// comment\nrepeated Foo test = 1;\n")
        .produce(
            () ->
                MessageFieldSpec.builder(FieldType.ENUM, "test", 1)
                    .setFieldComment("comment")
                    .setRepeated(true)
                    .setCustomTypeName("Foo")
                    .build());
  }

  @Test
  public void testExceptionCustomTypeNameUsage() throws IOException {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("custom type names only supported for MESSAGE and ENUM types");
    MessageFieldSpec.builder(FieldType.DOUBLE, "test", 1)
        .setCustomTypeName("Foo")
        .build()
        .emit(ProtoWriter.dud());
  }

  @Test
  public void testFieldOptions() {
    output
        .expects("// comment\nstring name = 1 [(foo) = true, (bar) = 56];\n")
        .produce(
            () ->
                MessageFieldSpec.builder(FieldType.STRING, "name", 1)
                    .setFieldComment("comment")
                    .addFieldOptions(
                        OptionSpec.builder(OptionType.FIELD, "foo").setValue(FieldType.BOOL, true),
                        OptionSpec.builder(OptionType.FIELD, "bar").setValue(FieldType.INT32, 56))
                    .build());
  }
}
