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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Models the concept of a Reservation in Protocol Buffers API.
 * Learn more: https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#reserved
 */
public final class ReservationSpec implements Emittable, Useable.FieldReservations, Buildable<ReservationSpec> {

  /** Creates a builder for a {@link ReservationSpec} using field numbers. */
  public static Builder builder(Integer... fieldNumbers) {
    return new Builder(ImmutableList.of(), ImmutableSet.copyOf(fieldNumbers).asList());
  }

  /** Creates a builder for a {@link ReservationSpec} using field names. */
  public static Builder builder(String... fieldNames) {
    return new Builder(ImmutableSet.copyOf(fieldNames).asList(), ImmutableList.of());
  }

  private final ImmutableList<String> fieldNames;
  private final ImmutableList<Integer> fieldNumbers;
  private final ImmutableList<FieldRange> fieldNumberRanges;
  private final ImmutableList<String> reservationComment;

  private ReservationSpec(Builder builder) {
    fieldNames = ImmutableList.copyOf(builder.fieldNames);
    fieldNumbers = ImmutableList.copyOf(builder.fieldNumbers);
    fieldNumberRanges = ImmutableList.copyOf(builder.fieldNumberRanges);
    reservationComment = ImmutableList.copyOf(builder.reservationComment);
  }

  @Override
  public void emit(ProtoWriter writer) throws IOException {
    if (!reservationComment.isEmpty()) {
      writer.emitComment(reservationComment);
    }
    Stream<String> names = fieldNames.stream().map(n -> String.format("\"%s\"", n));
    Stream<String> numbers = fieldNumbers.stream().map(String::valueOf);
    Stream<String> ranges = fieldNumberRanges.stream().map(FieldRange::toString);
    Stream<String> resos = Streams.concat(names, numbers, ranges);
    writer.emit(String.format("reserved %s;\n", resos.collect(Collectors.joining(", "))));
  }

  @Override
  public ReservationSpec build() {
    return this;
  }

  /** Returns a flattened stream of distinct field numbers that are represented  by this reservation. */
  @Override
  public IntStream asFieldNumberStream() {
    IntStream numbers = fieldNumbers.stream().mapToInt(Integer::intValue);
    IntStream ranges = fieldNumberRanges.stream().flatMapToInt(FieldRange::asStream);
    return Streams.concat(numbers, ranges).distinct();
  }

  /** Returns a stream of distinct field names represented by this reservation. */
  @Override
  public Stream<String> asFieldNameStream() {
    return fieldNames.stream();
  }

  /** Builder for producing new instances of {@link ReservationSpec}. */
  public static final class Builder implements Buildable<ReservationSpec> {

    private final ImmutableList<Integer> fieldNumbers;
    private final ImmutableList<String> fieldNames;
    private ImmutableList<String> reservationComment = ImmutableList.of();
    private ImmutableList<FieldRange> fieldNumberRanges = ImmutableList.of();
    
    private Builder(ImmutableList<String> fieldNames, ImmutableList<Integer> fieldNumbers) {
      boolean allNumbersPositive = fieldNumbers.stream().noneMatch(n -> n <= 0);
      checkArgument(allNumbersPositive, "negative field numbers are invalid");
      this.fieldNumbers = fieldNumbers;
      this.fieldNames = fieldNames;
    }

    /** Declares a comment above the reservation. */
    public Builder setReservationComment(Iterable<String> lines) {
      reservationComment = ImmutableList.copyOf(lines);
      return this;
    }

    /** Declares a comment above the reservation. */
    public Builder setReservationComment(String... lines) {
      return setReservationComment(ImmutableList.copyOf(lines));
    }

    /** Appends a field number range to the reservation, only valid for field number reservations. */
    public Builder addRanges(Iterable<FieldRange> rngs) {
      checkState(fieldNames.isEmpty(), "ranges are only allowed when reserving field numbers");
      fieldNumberRanges = ImmutableList.<FieldRange>builder()
        .addAll(fieldNumberRanges)
        .addAll(rngs)
        .build();
      return this;
    }

    /** Appends a field number range to the reservation, only valid for field number reservations. */
    public Builder addRanges(FieldRange... rngs) {
      return addRanges(ImmutableList.copyOf(rngs));
    }

    /** Builds a new instance of {@link ReservationSpec}. */
    @Override
    public ReservationSpec build() {
      return new ReservationSpec(this);
    }
  }
}
