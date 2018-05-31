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

public final class MessageSpecTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testWritingEmptyMessage() {
    output
      .expects("message TestMessage {}\n")
      .produce(() -> MessageSpec.builder("TestMessage").build());
  }

  @Test
  public void testWritingManyInnerMessages() {
    output
      .expectsTestData()
      .produce(() ->
               MessageSpec.builder("A")
               .addMessages(MessageSpec.builder("B")
                            .addMessages(MessageSpec.builder("C"),
                                         MessageSpec.builder("D")
                                         .addMessages(MessageSpec.builder("E"),
                                                      MessageSpec.builder("F"))),
                            MessageSpec.builder("G"))
               .build());
  }

  @Test
  public void testWritingMessageComment() {
    output
      .expects("// this is a test\nmessage A {}\n")
      .produce(() ->
               MessageSpec.builder("A")
               .setMessageComment("this is a test")
               .build());
  }

  @Test
  public void testWritingReservations() {
    output
      .expectsTestData()
      .produce(() ->
               MessageSpec.builder("A")
               .setMessageComment("comment")
               .addReservations(ReservationSpec.builder(1, 2).addRanges(FieldRange.of(3, 5)),
                                ReservationSpec.builder("foo","bar").setReservationComment("comment"))
               .build());
  }

  @Test
  public void testWritingMessageFields() {
    output
      .expectsTestData()
      .produce(() ->
               MessageSpec.builder("A")
               .setMessageComment("comment")
               .addMessageFields(MessageFieldSpec.builder(FieldType.BOOL, "a", 1),
                                 MessageFieldSpec.builder(FieldType.STRING, "b", 2)
                                 .setFieldComment("comment")
                                 .setRepeated(true),
                                 MessageFieldSpec.builder(FieldType.MESSAGE, "c", 3)
                                 .setCustomTypeName("Foo"))
               .build());
  }

  @Test
  public void testReservationEnforcement() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage("field number 1 is reserved and cannot be used");
    MessageSpec.builder("A")
      .addReservations(ReservationSpec.builder(1, 2))
      .addMessageFields(MessageFieldSpec.builder(FieldType.BOOL, "a", 1))
      .build()
      .emit(ProtoWriter.dud());
  }

  @Test
  public void testWritingOneof() {
    output
      .expectsTestData()
      .produce(() ->
               MessageSpec.builder("A")
               .setMessageComment("comment")
               .addMessageFields(MessageFieldSpec.builder(FieldType.STRING, "foo", 1),
                                 OneofFieldSpec.builder("B")
                                 .addMessageFields(MessageFieldSpec.builder(FieldType.BOOL, "bar", 2),
                                                   MessageFieldSpec.builder(FieldType.SFIXED32, "baz", 3)
                                                   .setFieldComment("comment")))
               .build());
  }

  @Test
  public void testEnsureOneofsRespectFieldUsages() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage("protopoet.UsageException: field number 1 already used by field named 'foo'");
    MessageSpec.builder("A")
      .addMessageFields(MessageFieldSpec.builder(FieldType.STRING, "foo", 1),
                        OneofFieldSpec.builder("B")
                        .addMessageFields(MessageFieldSpec.builder(FieldType.BOOL, "bar", 1)))
      .build()
      .emit(ProtoWriter.dud());
  }

  @Test
  public void testWritingMap() {
    output
      .expectsTestData()
      .produce(() ->
               MessageSpec.builder("A")
               .setMessageComment("comment")
               .addMessageFields(MessageFieldSpec.builder(FieldType.STRING, "foo", 1),
                                 MapFieldSpec.builder(FieldType.STRING, FieldType.STRING, "bar", 2)
                                 .setFieldComment("comment"))
               .build());
  }

  @Test
  public void testWritingMessageOptions() {
    output
      .expectsTestData()
      .produce(() ->
               MessageSpec.builder("A")
               .setMessageComment("comment")
               .addMessageOptions(OptionSpec.builder(OptionType.MESSAGE, "b")
                                  .setOptionComment("comment")
                                  .setValue(FieldType.STRING, "hello"),
                                  OptionSpec.builder(OptionType.MESSAGE, "c")
                                  .setOptionComment("comment")
                                  .setValue(FieldType.ENUM, "Test"))
               .build());
  }

  @Test
  public void testEnsureOptionTypeEnforced() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("option must be message type");
    MessageSpec.builder("A")
      .addMessageOptions(OptionSpec.builder(OptionType.ENUM, "b"));
  }

  @Test
  public void testEnsureUniqueInnerEntityNames() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage("'B' name already used in 'A'");
    MessageSpec.builder("A")
      .addMessages(MessageSpec.builder("B"))
      .addEnums(EnumSpec.builder("B"))
      .build()
      .emit(ProtoWriter.dud());
  }
}
