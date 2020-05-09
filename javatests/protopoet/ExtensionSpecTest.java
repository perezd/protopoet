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

public final class ExtensionSpecTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testWritingEmptyExtensionWithComment() {
    output
        .expects("// this is a comment\nextend google.protobuf.FileOptions {}\n")
        .produce(
            () ->
                ExtensionSpec.builder(OptionType.FILE)
                    .setExtensionComment("this is a comment")
                    .build());
  }

  @Test
  public void testWritingFields() {
    output
        .expectsTestData()
        .produce(
            () ->
                ExtensionSpec.builder(OptionType.SERVICE)
                    .setExtensionComment("comment")
                    .addExtensionFields(
                        MessageFieldSpec.builder(FieldType.STRING, "test", 1),
                        MessageFieldSpec.builder(FieldType.BOOL, "bar", 2)
                            .setFieldComment("comment"))
                    .build());
  }

  @Test
  public void testEnsureMapsNotAllowed() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("complex fields not allowed (eg: oneofs or maps)");
    ExtensionSpec.builder(OptionType.FILE)
        .addExtensionFields(MapFieldSpec.builder(FieldType.BOOL, FieldType.BOOL, "test", 1))
        .build();
  }

  @Test
  public void testEnsureOneofsNotAllowed() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("complex fields not allowed (eg: oneofs or maps)");
    ExtensionSpec.builder(OptionType.FILE)
        .addExtensionFields(OneofFieldSpec.builder("test"))
        .build();
  }

  @Test
  public void testEnsureFieldUniqueness() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage(
        "protopoet.UsageException: field name 'test' (number=1) not unique, used by field number 1");
    ExtensionSpec.builder(OptionType.FILE)
        .addExtensionFields(MessageFieldSpec.builder(FieldType.BOOL, "test", 1))
        .addExtensionFields(MessageFieldSpec.builder(FieldType.BOOL, "test", 1))
        .build()
        .emit(ProtoWriter.dud());
  }
}
