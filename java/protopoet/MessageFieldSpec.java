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
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Optional;

/**
 * Defines a field for use with {@link MessageSpec}. Learn more:
 * https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#message_definition
 */
public final class MessageFieldSpec implements MessageField, Useable.Field {

  /** Creates a builder for a {@link MessageFieldSpec}. */
  public static Builder builder(FieldType fieldType, String fieldName, int fieldNumber) {
    checkNotNull(fieldType, "field type may not be null");
    checkNotNull(fieldName, "field name may not be null");
    checkArgument(fieldNumber > 0, "field number must be positive");
    return new Builder(fieldType, fieldName, fieldNumber);
  }

  /** Shortcut builder for a repeated {@link MessageFieldSpec}. */
  public static Builder repeated(FieldType fieldType, String fieldName, int fieldNumber) {
    Builder builder = builder(fieldType, fieldName, fieldNumber);
    builder.setRepeated(true);
    return builder;
  }

  /** Shortcut builder for an optional {@link MessageFieldSpec}. */
  public static Builder optional(FieldType fieldType, String fieldName, int fieldNumber) {
    Builder builder = builder(fieldType, fieldName, fieldNumber);
    builder.setOptional(true);
    return builder;
  }

  /** Shortcut builder for a message-typed {@link MessageFieldSpec}. */
  public static Builder message(String typeName, String fieldName, int fieldNumber) {
    Builder builder = builder(FieldType.MESSAGE, fieldName, fieldNumber);
    builder.setCustomTypeName(typeName);
    return builder;
  }

  private final FieldType fieldType;
  private final String fieldName;
  private final int fieldNumber;
  private final ImmutableList<String> fieldComment;
  private final boolean isRepeated;
  private final boolean isOptional;
  private final String customTypeName;
  private final ImmutableList<OptionSpec> options;

  private MessageFieldSpec(Builder builder) {
    fieldType = builder.fieldType;
    fieldName = builder.fieldName;
    fieldNumber = builder.fieldNumber;
    fieldComment = ImmutableList.copyOf(builder.fieldComment);
    isRepeated = builder.isRepeated;
    isOptional = builder.isOptional;
    customTypeName = builder.customTypeName;
    options = ImmutableList.copyOf(builder.options);
  }

  @Override
  public void emit(ProtoWriter writer) throws IOException {
    if (!fieldComment.isEmpty()) {
      writer.emitComment(fieldComment);
    }

    // enums and messages are special and require a provided
    // explicit type name.
    String actualFieldType = fieldType.toString();
    if (fieldType == FieldType.ENUM || fieldType == FieldType.MESSAGE) {
      checkNotNull(
          customTypeName,
          String.format(
              "'%s' type is %s and needs a custom type name associated with it",
              fieldName, fieldType));
      actualFieldType = customTypeName;
    }

    String prefix = "";
    if (isRepeated) {
      prefix = "repeated ";
    } else if (isOptional) {
      prefix = "optional ";
    }

    writer.emit(String.format("%s%s %s = %d", prefix, actualFieldType, fieldName, fieldNumber));
    OptionSpec.emitFieldOptions(options, writer).emit(";\n");
  }

  @Override
  public String fieldName() {
    return fieldName;
  }

  @Override
  public Optional<Integer> fieldNumber() {
    return Optional.of(fieldNumber);
  }

  @Override
  public MessageField build() {
    return this;
  }

  boolean isRepeated() {
    return isRepeated;
  }

  /** Builder for a {@link MessageFieldSpec}. */
  public static final class Builder implements Buildable<MessageField> {

    private final FieldType fieldType;
    private final String fieldName;
    private final int fieldNumber;
    private ImmutableList<String> fieldComment = ImmutableList.of();
    private boolean isRepeated;
    private boolean isOptional;
    private String customTypeName = null;
    private ImmutableList<OptionSpec> options = ImmutableList.of();

    private Builder(FieldType fieldType, String fieldName, int fieldNumber) {
      this.fieldType = fieldType;
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

    /** Designates whether or not a field should be identified as repeatable. */
    public Builder setRepeated(boolean isRepeated) {
      checkState(!isOptional, "optional fields cannot be repeated");
      this.isRepeated = isRepeated;
      return this;
    }

    /** Declares a custom type name. For use when field is type is a Message or Enum. */
    public Builder setCustomTypeName(String customTypeName) {
      checkNotNull(customTypeName, "custom type name may not be null");
      checkState(
          fieldType == FieldType.MESSAGE || fieldType == FieldType.ENUM,
          "custom type names only supported for MESSAGE and ENUM types");
      this.customTypeName = customTypeName;
      return this;
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
                              opt.optionType() == OptionType.FIELD, "option must be field type")))
              .build();
      return this;
    }

    /** Adds options to the field. See {@link OptionSpec}. */
    @SafeVarargs
    public final Builder addFieldOptions(Buildable<OptionSpec>... options) {
      return addFieldOptions(ImmutableList.copyOf(options));
    }

    /**
     * Designates whether or not a field should track explicit presence. Learn more about usage:
     * https://github.com/protocolbuffers/protobuf/blob/master/docs/field_presence.md
     */
    public Builder setOptional(boolean isOptional) {
      checkState(!isRepeated, "repeated fields cannot be explicitly optional");
      this.isOptional = isOptional;
      return this;
    }

    /** Builds a new instance of {@link MessageFieldSpec}. */
    @Override
    public MessageField build() {
      return new MessageFieldSpec(this);
    }
  }
}
