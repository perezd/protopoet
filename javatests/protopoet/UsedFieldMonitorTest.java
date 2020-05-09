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

import java.util.Collections;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class UsedFieldMonitorTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private UsedFieldMonitor monitor;

  @Before
  public void setUp() {
    monitor = new UsedFieldMonitor();
  }

  @Test
  public void testEmptyUnused() throws UsageException {
    // Should not throw.
    monitor.ensureUnused(new FakeField("a", 1));
    assertThat(true).isTrue();
  }

  @Test
  public void testExceptionUsedFieldName() throws UsageException {
    thrown.expect(UsageException.class);
    thrown.expectMessage("field name 'a' (number=2) not unique, used by field number 1");
    monitor.add(new FakeField("a", 1));
    monitor.ensureUnused(new FakeField("a", 2));
  }

  @Test
  public void testExceptionUsedFieldNumber() throws UsageException {
    thrown.expect(UsageException.class);
    thrown.expectMessage("field number 1 already used by field named 'a'");
    monitor.add(new FakeField("a", 1));
    monitor.ensureUnused(new FakeField("b", 1));
  }

  @Test
  public void testExceptionFieldNumberReservations() throws UsageException {
    thrown.expect(UsageException.class);
    thrown.expectMessage("field number 1 is reserved and cannot be used");
    Useable.FieldReservations reservations =
        new Useable.FieldReservations() {
          @Override
          public IntStream asFieldNumberStream() {
            return IntStream.of(1, 4, 5);
          }

          @Override
          public Stream<String> asFieldNameStream() {
            return Stream.of();
          }
        };
    monitor.add(reservations);
    monitor.ensureUnused(new FakeField("a", 1));
  }

  @Test
  public void testExceptionFieldNameReservations() throws UsageException {
    thrown.expect(UsageException.class);
    thrown.expectMessage("field name 'a' is reserved and cannot be used");
    Useable.FieldReservations reservations =
        new Useable.FieldReservations() {
          @Override
          public IntStream asFieldNumberStream() {
            return IntStream.of();
          }

          @Override
          public Stream<String> asFieldNameStream() {
            return Stream.of("a");
          }
        };
    monitor.add(reservations);
    monitor.ensureUnused(new FakeField("a", 1));
  }

  @Test
  public void testResetState() throws UsageException {
    monitor.add(new FakeField("a", 1));
    monitor.reset();
    monitor.add(new FakeField("a", 1));
  }

  @Test
  public void testUseableFieldsFieldNameReserved() throws UsageException {
    thrown.expect(UsageException.class);
    thrown.expectMessage("field name 'test' is not unique");
    monitor.add(
        new Useable.Fields() {
          @Override
          public String fieldName() {
            return "test";
          }

          @Override
          public Iterable<Useable.Field> fields() {
            return Collections.EMPTY_LIST;
          }
        });
    monitor.add(new FakeField("test", 2));
  }

  // Stub for testing purposes.
  static final class FakeField implements Useable.Field {

    private final String fieldName;
    private final int fieldNumber;

    FakeField(String fieldName, int fieldNumber) {
      this.fieldName = fieldName;
      this.fieldNumber = fieldNumber;
    }

    @Override
    public String fieldName() {
      return fieldName;
    }

    @Override
    public Optional<Integer> fieldNumber() {
      return Optional.of(fieldNumber);
    }
  }
}
