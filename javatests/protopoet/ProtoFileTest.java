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

public final class ProtoFileTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testWritingImports() {
    output
        .expectsTestData()
        .produce(
            () ->
                ProtoFile.builder()
                    .addImports(
                        ImportSpec.of("test.proto"),
                        ImportSpec.of(ImportSpec.Modifier.PUBLIC, "foo.proto"),
                        ImportSpec.of(ImportSpec.Modifier.WEAK, "bar.proto"))
                    .build());
  }

  @Test
  public void testWritingPackageName() {
    output
        .expectsTestData()
        .produce(() -> ProtoFile.builder().setPackageName("com.whatever").build());
  }

  @Test
  public void testWritingFileComment() {
    output
        .expectsTestData()
        .produce(
            () ->
                ProtoFile.builder()
                    .setFileComment("this is a test of my ability", "to write a file comment")
                    .build());
  }

  @Test
  public void testWritingEmptyNestedMessages() {
    output
        .expectsTestData()
        .produce(
            () ->
                ProtoFile.builder()
                    .setFileComment(
                        "this is a test of writing multiple top-level messages",
                        "with nested messages inside of them.")
                    .setPackageName("com.testing")
                    .addMessages(MessageSpec.builder("A").setMessageComment("this has a comment"))
                    .addMessages(
                        MessageSpec.builder("B")
                            .addMessages(
                                MessageSpec.builder("C"),
                                MessageSpec.builder("D")
                                    .setMessageComment("this one has a comment")))
                    .build());
  }

  @Test
  public void testWritingEnums() {
    output
        .expectsTestData()
        .produce(
            () ->
                ProtoFile.builder()
                    .setFileComment("Tests writing top level and nested enums")
                    .setPackageName("com.testing")
                    .addEnums(
                        EnumSpec.builder("TopLevel")
                            .setEnumComment("This is toplevel")
                            .addReservations(
                                ReservationSpec.builder(3, 5).setReservationComment("reserved"))
                            .addEnumFields(
                                EnumFieldSpec.builder("A", 1), EnumFieldSpec.builder("B", 2)))
                    .addMessages(
                        MessageSpec.builder("TopLevelMsg")
                            .addEnums(
                                EnumSpec.builder("InnerEnum")
                                    .setEnumComment("this is inside")
                                    .addReservations(
                                        ReservationSpec.builder("foo")
                                            .setReservationComment("reserved"))
                                    .addEnumFields(
                                        EnumFieldSpec.builder("C", 1),
                                        EnumFieldSpec.builder("D", 2)
                                            .setFieldComment("field comment"))))
                    .build());
  }

  @Test
  public void testWritingExtensions() {
    output
        .expectsTestData()
        .produce(
            () ->
                ProtoFile.builder()
                    .setFileComment("comment")
                    .setPackageName("com.testing")
                    .addExtensions(
                        ExtensionSpec.builder(OptionType.SERVICE)
                            .setExtensionComment("comment")
                            .addExtensionFields(
                                MessageFieldSpec.builder(FieldType.BOOL, "foo", 1),
                                MessageFieldSpec.builder(FieldType.MESSAGE, "bar", 2)
                                    .setCustomTypeName("Bar")
                                    .setFieldComment("comment")))
                    .build());
  }

  @Test
  public void testWritingServices() {
    output
        .expectsTestData()
        .produce(
            () ->
                ProtoFile.builder()
                    .setFileComment("comment")
                    .setPackageName("com.testing")
                    .addServices(
                        ServiceSpec.builder("A")
                            .setServiceComment("comment")
                            .addRpcFields(
                                RpcFieldSpec.builder("B")
                                    .setFieldComment("comment")
                                    .setRequestMessageName("Req", true)
                                    .setResponseMessageName("Res")))
                    .build());
  }

  @Test
  public void testWritingFileOptions() {
    output
        .expectsTestData()
        .produce(
            () ->
                ProtoFile.builder()
                    .setFileComment("comment")
                    .addFileOptions(
                        OptionSpec.builder(OptionType.FILE, "b")
                            .setOptionComment("comment")
                            .setValue(FieldType.STRING, "hello"),
                        OptionSpec.builder(OptionType.FILE, "c")
                            .setOptionComment("comment")
                            .setValue(FieldType.BOOL, true))
                    .build());
  }

  @Test
  public void testEnsureOptionTypeEnforced() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("option must be file type");
    ProtoFile.builder().addFileOptions(OptionSpec.builder(OptionType.MESSAGE, "b"));
  }

  @Test
  public void testEnsureNameUniquenessEnforced() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage("'B' name already used");
    ProtoFile.builder()
        .addMessages(MessageSpec.builder("A"))
        .addEnums(EnumSpec.builder("B"))
        .addServices(ServiceSpec.builder("B"))
        .build()
        .emit(ProtoWriter.dud());
  }
}
