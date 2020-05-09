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

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Streams;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Monitor class that helps language emitters keep track of used and acceptable to use fields (by
 * exclusion via reservations). Checkout {@link Useable} interface for particular APIs that need to
 * be configured to make use of this.
 */
final class UsedFieldMonitor {

  private final HashBiMap<String, Integer> fields = HashBiMap.create();
  private final AtomicInteger unknownFieldNumberOffset = new AtomicInteger(0);
  private final HashSet<Integer> reservedFieldNumbers = new HashSet<>();

  /** Adds a reservation of field names or numbers that should not be allowed for usage. */
  void add(Useable.FieldReservations usableFieldSet) throws UsageException {
    Stream<Useable.Field> nums =
        usableFieldSet.asFieldNumberStream().mapToObj(this::toReservedField);
    Stream<Useable.Field> names = usableFieldSet.asFieldNameStream().map(this::toReservedField);

    // This form of iteration is required because we may throw an exception.
    List<Useable.Field> resos = Streams.concat(nums, names).collect(Collectors.toList());
    for (Useable.Field reso : resos) {
      add(reso);
      // We must also record the reserved field number for double checking later.
      // Reason being, we generated an unknown field number offset when this is called,
      // and later we need something to disambiguate between a field that was actively reserved
      // vs a field that doesn't provide us with a field number.
      reservedFieldNumbers.add(reso.fieldNumber().get());
    }
  }

  /** Adds a field that has been used to prevent future usages. */
  void add(Useable.Field useableField) throws UsageException {
    ensureUnused(useableField);
    // If we don't get a reliable field number back, we can use our unknown field number generator
    // to ensure we have unique bimap entries.
    fields.put(
        useableField.fieldName(),
        useableField.fieldNumber().orElseGet(unknownFieldNumberOffset::decrementAndGet));
  }

  /** Adds all fields found within a container/wrapper. */
  void add(Useable.Fields useableFields) throws UsageException {
    // First, add the name of the block of fields in this case.
    add(toReservedField(useableFields.fieldName()));
    for (Useable.Field field : useableFields.fields()) {
      add(field);
    }
  }

  /** Clears all known state for the monitor. */
  void reset() {
    fields.clear();
    reservedFieldNumbers.clear();
    unknownFieldNumberOffset.set(0);
  }

  /**
   * Verifies that a field hasn't be used before with this instance of the monitor. Throws {@link
   * UsageException} iif that is not true.
   */
  void ensureUnused(Useable.Field useableField) throws UsageException {
    String fieldName = useableField.fieldName();
    Optional<Integer> fieldNumber = useableField.fieldNumber();

    // Ensure that a given field name isn't reused within this scope.
    if (fields.containsKey(fieldName)) {
      int usedFieldNumber = fields.get(fieldName);
      if (usedFieldNumber < 0) {
        // If the number is negative (unknown) and has been positively captured
        // as a reserved field number, this is the message to display.
        if (reservedFieldNumbers.contains(usedFieldNumber)) {
          throw new UsageException(
              String.format("field name '%s' is reserved and cannot be used", fieldName));
        } else {
          // Otherwise, its a reused field that does not have a number to verify, so we can
          // assert the field name has been used, regardless.
          throw new UsageException(String.format("field name '%s' is not unique", fieldName));
        }
      } else {
        // In this case, the field name is not unique and we can actually tell
        // the user what field number is using the field.
        throw new UsageException(
            String.format(
                "field name '%s' (number=%d) not unique, used by field number %d",
                fieldName, fieldNumber.get(), usedFieldNumber));
      }
    }

    // Bail if we don't have a field number.
    if (!fieldNumber.isPresent()) {
      return;
    }

    // Ensure that a given field number isn't reused within this scope.
    int fieldNum = fieldNumber.get();
    if (fields.containsValue(fieldNum)) {
      String usedFieldName = fields.inverse().get(fieldNum);
      if (usedFieldName.startsWith("__RESERVED_")) {
        throw new UsageException(
            String.format("field number %d is reserved and cannot be used", fieldNum));
      } else {
        throw new UsageException(
            String.format(
                "field number %d already used by field named '%s'", fieldNum, usedFieldName));
      }
    }
  }

  UsedFieldMonitor() {}

  private Useable.Field toReservedField(final Integer fieldNumber) {
    return new Useable.Field() {
      @Override
      public String fieldName() {
        return String.format("__RESERVED_%d", fieldNumber);
      }

      @Override
      public Optional<Integer> fieldNumber() {
        return Optional.of(fieldNumber);
      }
    };
  }

  private Useable.Field toReservedField(final String fieldName) {
    // Here we must make up a field number using our unknown field number generator.
    Integer reservedFieldNum = unknownFieldNumberOffset.decrementAndGet();
    return new Useable.Field() {
      @Override
      public String fieldName() {
        return fieldName;
      }

      @Override
      public Optional<Integer> fieldNumber() {
        return Optional.of(reservedFieldNum);
      }
    };
  }
}
