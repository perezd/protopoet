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
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.io.IOException;

/**
 * Defines an enum type for the Protocol Buffer language. Also see: {@link EnumFieldSpec}.
 * More info: https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#enum_definition
 */
public final class EnumSpec implements Emittable, Buildable<EnumSpec>, Useable.Name {

  /** Creates a builder for an {@link EnumSpec}. */
  public static Builder builder(String enumName) {
    checkNotNull(enumName, "enum name may not be null");
    return new Builder(enumName);
  }

  private final String enumName;
  private final ImmutableList<String> enumComment;  
  private final ImmutableList<EnumFieldSpec> enumFields;
  private final ImmutableList<ReservationSpec> reservations;
  private final ImmutableList<OptionSpec> options;
  private final UsedFieldMonitor usedFieldMonitor = new UsedFieldMonitor();  
  
  private EnumSpec(Builder builder) {
    enumName = builder.enumName;
    enumComment = ImmutableList.copyOf(builder.enumComment);
    enumFields = ImmutableList.copyOf(builder.enumFields);
    reservations = ImmutableList.copyOf(builder.reservations);
    options = ImmutableList.copyOf(builder.options);
  }
  
  @Override
  public void emit(ProtoWriter writer) throws IOException {
    usedFieldMonitor.reset();
    if (!enumComment.isEmpty()) {
      writer.emitComment(enumComment);
    }
    writer.emit(String.format("enum %s {", enumName));

    // Options are hoisted to the very top of the enum for clarity/consistency.
    if (!options.isEmpty()) {
      writer
        .emit("\n")
        .indent();
      for (OptionSpec option : options) {
        option.emit(writer);
      }
      writer.unindent();
    }

    // Reservations are hoisted to the top as well, and added to the UsedFieldMonitor
    // first so we can track if the following src blocks accidentally use them.
    if (!reservations.isEmpty()) {
      writer
        .emit("\n")
        .indent();
      for (ReservationSpec reservation : reservations) {
        try {
          usedFieldMonitor.add(reservation);
          reservation.emit(writer);
        } catch (UsageException ex) {
          throw new IOException(ex);
        }
      }
      writer.unindent();
    }

    // Finally, writes all the known enum fields into the enun,
    // tracking used fields from being reused.
    if (!enumFields.isEmpty()) {
      writer
        .emit("\n")
        .indent();
      for (EnumFieldSpec enumField : enumFields) {
        try {
          usedFieldMonitor.add(enumField);
          enumField.emit(writer);
        } catch (UsageException ex) {
          throw new IOException(ex);
        }
      }
      writer.unindent();
    }
    writer.emit("}\n");
  }

  @Override
  public EnumSpec build() {
    return this;
  }

  @Override
  public String name() {
    return enumName;
  }

  /** Builder for an {@link EnumSpec}. */
  public static final class Builder implements Buildable<EnumSpec> {

    private final String enumName;
    private ImmutableList<String> enumComment = ImmutableList.of();
    private ImmutableList<EnumFieldSpec> enumFields = ImmutableList.of();
    private ImmutableList<ReservationSpec> reservations = ImmutableList.of();
    private ImmutableList<OptionSpec> options = ImmutableList.of();
    
    private Builder(String enumName) {
      this.enumName = enumName;
    }

    /** Declares a top-level comment for the enum. */
    public Builder setEnumComment(Iterable<String> lines) {
      enumComment = ImmutableList.copyOf(lines);
      return this;
    }

    /** Declares a top-level comment for the enum. */
    public Builder setEnumComment(String... lines) {
      return setEnumComment(ImmutableList.copyOf(lines));
    }

    /** Adds fields to the enum. See {@link EnumFieldSpec}. */
    public Builder addEnumFields(Iterable<? extends Buildable<EnumFieldSpec>> fields) {
      enumFields = ImmutableList.<EnumFieldSpec>builder()
        .addAll(enumFields)
        .addAll(Buildables.buildAll(fields))
        .build();
      return this;
    }

    /** Adds fields to the enum. See {@link EnumFieldSpec}. */
    @SafeVarargs
    public final Builder addEnumFields(Buildable<EnumFieldSpec>... fields) {
      return addEnumFields(ImmutableList.copyOf(fields));
    }

    /** Adds field reservations for the enum, see {@link ReservationSpec}. */
    public Builder addReservations(Iterable<Buildable<ReservationSpec>> resos) {
      reservations = ImmutableList.<ReservationSpec>builder()
        .addAll(reservations)
        .addAll(Buildables.buildAll(resos))
        .build();
      return this;
    }

    /** Adds field reservations for the enum, see {@link ReservationSpec}. */
    public Builder addReservations(Buildable<ReservationSpec>... resos) {
      return addReservations(ImmutableList.copyOf(resos));
    }

    /** Adds options to the enum. See {@link OptionSpec}. */
    public Builder addEnumOptions(Iterable<? extends Buildable<OptionSpec>> options) {
      this.options = ImmutableList.<OptionSpec>builder()
        .addAll(this.options)
        .addAll(Buildables.buildAll(options,
                                   opt -> checkArgument(opt.optionType() == OptionType.ENUM,
                                                     "option must be enum type")))
        .build();
      return this;
    }

    /** Adds options to the enum. See {@link OptionSpec}. */
    public Builder addEnumOptions(Buildable<OptionSpec>... options) {
      return addEnumOptions(ImmutableList.copyOf(options));
    }

    /** Builds a new instance of {@link EnumSpec}. */
    @Override
    public EnumSpec build() {
      return new EnumSpec(this);
    }
  }
}
