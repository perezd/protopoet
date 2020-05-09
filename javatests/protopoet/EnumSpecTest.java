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

public final class EnumSpecTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testWritingEmptyEnumWithComment() {
    output
        .expects("// this is a comment\nenum A {}\n")
        .produce(() -> EnumSpec.builder("A").setEnumComment("this is a comment").build());
  }

  @Test
  public void testWritingEnumWithFields() {
    output
        .expectsTestData()
        .produce(
            () ->
                EnumSpec.builder("A")
                    .setEnumComment("comment")
                    .addEnumFields(
                        EnumFieldSpec.builder("A", 1).setFieldComment("field 1"),
                        EnumFieldSpec.builder("B", 2),
                        EnumFieldSpec.builder("C", 3))
                    .build());
  }

  @Test
  public void ensureUniqueFieldNames() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage("field name 'A' (number=2) not unique, used by field number 1");
    EnumSpec.builder("A")
        .addEnumFields(EnumFieldSpec.builder("A", 1), EnumFieldSpec.builder("A", 2))
        .build()
        .emit(ProtoWriter.dud());
  }

  @Test
  public void ensureUniqueFieldNumbers() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage("field number 1 already used by field named 'A'");
    EnumSpec.builder("A")
        .addEnumFields(EnumFieldSpec.builder("A", 1), EnumFieldSpec.builder("B", 1))
        .build()
        .emit(ProtoWriter.dud());
  }

  @Test
  public void ensureEnforcementOfFieldNumberReservations() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage("field number 1 is reserved and cannot be used");
    EnumSpec.builder("A")
        .addReservations(ReservationSpec.builder(1))
        .addEnumFields(EnumFieldSpec.builder("A", 1), EnumFieldSpec.builder("B", 2))
        .build()
        .emit(ProtoWriter.dud());
  }

  @Test
  public void ensureEnforcementOfFieldNameReservations() throws IOException {
    thrown.expect(IOException.class);
    thrown.expectMessage("field name 'B' is reserved and cannot be used");
    EnumSpec.builder("A")
        .addReservations(ReservationSpec.builder("B"))
        .addEnumFields(EnumFieldSpec.builder("A", 1), EnumFieldSpec.builder("B", 2))
        .build()
        .emit(ProtoWriter.dud());
  }

  @Test
  public void testWritingReservations() {
    output
        .expectsTestData()
        .produce(
            () ->
                EnumSpec.builder("A")
                    .setEnumComment("comment")
                    .addReservations(
                        ReservationSpec.builder(1, 2).addRanges(FieldRange.of(3, 5)),
                        ReservationSpec.builder("foo", "bar").setReservationComment("comment"))
                    .addEnumFields(
                        EnumFieldSpec.builder("A", 10).setFieldComment("field 1"),
                        EnumFieldSpec.builder("B", 11),
                        EnumFieldSpec.builder("C", 12))
                    .build());
  }

  @Test
  public void testWritingEnumOptions() {
    output
        .expectsTestData()
        .produce(
            () ->
                EnumSpec.builder("A")
                    .setEnumComment("comment")
                    .addEnumOptions(
                        OptionSpec.builder(OptionType.ENUM, "b")
                            .setOptionComment("comment")
                            .setValue(FieldType.INT32, 12345),
                        OptionSpec.builder(OptionType.ENUM, "c")
                            .setOptionComment("comment")
                            .setValue(FieldType.BOOL, false))
                    .build());
  }

  @Test
  public void testEnsureOptionTypeEnforced() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("option must be enum type");
    EnumSpec.builder("A").addEnumOptions(OptionSpec.builder(OptionType.MESSAGE, "b"));
  }
}
