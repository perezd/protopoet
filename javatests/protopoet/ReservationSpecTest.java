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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class ReservationSpecTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();
  
  @Test
  public void testFieldNumberReservations() {
    output
      .expects("// hello\nreserved 1, 2, 3, 5, 8;\n")
      .produce(() ->
               ReservationSpec.builder(1, 2, 3, 5, 8)
               .setReservationComment("hello")
               .build());
  }

  @Test
  public void testFieldNameReservations() {
    output
      .expects("// hello\nreserved \"foo\", \"bar\";\n")
      .produce(() ->
               ReservationSpec.builder("foo", "bar")
               .setReservationComment("hello")
               .build());
  }

  @Test
  public void testFieldNumberRanges() {
    output
      .expects("// hello\nreserved 1, 2, 3, 4 to 8, 9 to 10;\n")
      .produce(()-> ReservationSpec.builder(1,2,3)
               .setReservationComment("hello")
               .addRanges(FieldRange.of(4, 8),
                          FieldRange.of(9, 10))
               .build());
  }

  @Test
  public void testFieldNumberRangesInvalidWithFieldNames() throws IOException {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("ranges are only allowed when reserving field numbers");
    ReservationSpec.builder("should", "fail")
      .addRanges(FieldRange.of(4, 8),
                 FieldRange.of(9, 10))
      .build()
      .emit(ProtoWriter.dud());
  }

  @Test
  public void testRangeEnforcement() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("high value must be higher than low value");
    FieldRange.of(2, 1);
  }

  @Test
  public void testTagNumberStream() {
    IntStream tagNumStream = ReservationSpec.builder(1, 2, 3)
      .addRanges(FieldRange.of(10, 15),
                 FieldRange.of(5, 8))
      .build()
      .asFieldNumberStream()
      .sorted();
    assertThat(tagNumStream.toArray())
      .isEqualTo(new int[]{1, 2, 3, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15});
  }

  @Test
  public void testFieldNameStream() {
    List<String> fieldNames = ReservationSpec.builder("a", "b", "c")
      .build()
      .asFieldNameStream()
      .collect(Collectors.toList());
    assertThat(Arrays.asList("a", "b", "c")).isEqualTo(fieldNames);
  }

  @Test
  public void ensureFieldNumberMustBePositive() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("negative field numbers are invalid");
    ReservationSpec.builder(1, 2, -4);
  }
}
