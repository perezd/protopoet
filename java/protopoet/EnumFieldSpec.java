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
import java.util.Optional;

/**
 * Defines a field for use with {@link EnumSpec}. Learn more:
 * https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#enum_definition
 */
public final class EnumFieldSpec implements Emittable, Useable.Field, Buildable<EnumFieldSpec> {

  /** Creates a builder for an {@link EnumFieldSpec}. */
  public static Builder builder(String fieldName, int fieldNumber) {
    checkNotNull(fieldName, "enum field name may not be null");
    checkArgument(fieldNumber >= 0, "field number may not be negative");
    return new Builder(fieldName, fieldNumber);
  }

  private final String fieldName;
  private final int fieldNumber;
  private final ImmutableList<String> fieldComment;
  private final ImmutableList<OptionSpec> options;

  private EnumFieldSpec(Builder builder) {
    fieldName = builder.fieldName;
    fieldNumber = builder.fieldNumber;
    fieldComment = ImmutableList.copyOf(builder.fieldComment);
    options = ImmutableList.copyOf(builder.options);
  }

  @Override
  public void emit(ProtoWriter writer) throws IOException {
    if (!fieldComment.isEmpty()) {
      writer.emitComment(fieldComment);
    }
    writer.emit(String.format("%s = %d", fieldName, fieldNumber));
    OptionSpec.emitFieldOptions(options, writer).emit(";\n");
  }

  @Override
  public EnumFieldSpec build() {
    return this;
  }

  @Override
  public String fieldName() {
    return fieldName;
  }

  @Override
  public Optional<Integer> fieldNumber() {
    return Optional.of(fieldNumber);
  }

  /** Builder for a {@link EnumFieldSpec}. */
  public static final class Builder implements Buildable<EnumFieldSpec> {

    private final String fieldName;
    private final int fieldNumber;
    private ImmutableList<String> fieldComment = ImmutableList.of();
    private ImmutableList<OptionSpec> options = ImmutableList.of();

    private Builder(String fieldName, int fieldNumber) {
      this.fieldName = fieldName;
      this.fieldNumber = fieldNumber;
    }

    /** Declares a comment for the field. */
    public Builder setFieldComment(Iterable<String> lines) {
      fieldComment = ImmutableList.copyOf(lines);
      return this;
    }

    /** Declares a comment for the field. */
    public Builder setFieldComment(String... lines) {
      return setFieldComment(ImmutableList.copyOf(lines));
    }

    /** Adds options to the field. See {@link OptionSpec}. */
    public Builder addFieldOptions(Iterable<? extends Buildable<OptionSpec>> options) {
      this.options =
          ImmutableList.<OptionSpec>builder()
              .addAll(this.options)
              .addAll(
                  Buildables.buildAll(
                      options,
                      opt ->
                          checkArgument(
                              opt.optionType() == OptionType.ENUM_VALUE,
                              "option must be enum value type")))
              .build();
      return this;
    }

    /** Adds options to the field. See {@link OptionSpec}. */
    @SafeVarargs
    public final Builder addFieldOptions(Buildable<OptionSpec>... options) {
      return addFieldOptions(ImmutableList.copyOf(options));
    }

    /** Builds a new instance of {@link EnumFieldSpec}. */
    @Override
    public EnumFieldSpec build() {
      return new EnumFieldSpec(this);
    }
  }
}
