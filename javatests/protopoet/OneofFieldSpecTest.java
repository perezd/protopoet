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

public final class OneofFieldSpecTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testWritingEmptyOneof() {
    output
        .expects("// comment\noneof A {}\n")
        .produce(() -> OneofFieldSpec.builder("A").setFieldComment("comment").build());
  }

  @Test
  public void testWritingOneofFields() {
    output
        .expectsTestData()
        .produce(
            () ->
                OneofFieldSpec.builder("A")
                    .setFieldComment("comment")
                    .addMessageFields(
                        MessageFieldSpec.builder(FieldType.BOOL, "b", 1),
                        MessageFieldSpec.builder(FieldType.STRING, "c", 2)
                            .setFieldComment("comment"))
                    .build());
  }

  @Test
  public void ensureRepeatedFieldsDisallowed() throws IOException {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("repeated field 'b' not allowed in oneof");
    OneofFieldSpec.builder("A")
        .setFieldComment("comment")
        .addMessageFields(MessageFieldSpec.builder(FieldType.BOOL, "b", 1).setRepeated(true))
        .build()
        .emit(ProtoWriter.dud());
  }

  @Test
  public void ensureOneofFieldsRecursivelyDisallowed() throws IOException {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("immediate inner oneof field 'B' disallowed");
    OneofFieldSpec.builder("A")
        .setFieldComment("comment")
        .addMessageFields(OneofFieldSpec.builder("B"))
        .build()
        .emit(ProtoWriter.dud());
  }

  @Test
  public void ensureMapFieldDisallowed() throws IOException {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("map field 'b' not allowed in oneof");
    OneofFieldSpec.builder("A")
        .setFieldComment("comment")
        .addMessageFields(MapFieldSpec.builder(FieldType.BOOL, FieldType.BOOL, "b", 1))
        .build()
        .emit(ProtoWriter.dud());
  }

  @Test
  public void testWritingOneofOptions() {
    output
        .expectsTestData()
        .produce(
            () ->
                OneofFieldSpec.builder("A")
                    .setFieldComment("comment")
                    .addOneofOptions(
                        OptionSpec.builder(OptionType.ONEOF, "b")
                            .setOptionComment("comment")
                            .setValue(FieldType.INT32, 12345),
                        OptionSpec.builder(OptionType.ONEOF, "c")
                            .setOptionComment("comment")
                            .setValue(FieldType.BOOL, false))
                    .build());
  }

  @Test
  public void testEnsureOptionTypeEnforced() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("option must be oneof type");
    OneofFieldSpec.builder("A").addOneofOptions(OptionSpec.builder(OptionType.MESSAGE, "b"));
  }
}
