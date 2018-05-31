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
import java.util.EnumSet;
import java.util.Optional;

/**
 * Defines a map field for use with {@link MessageSpec}.
 * Learn more: https://developers.google.com/protocol-buffers/docs/proto3#maps
 */
public final class MapFieldSpec implements MessageField, Useable.Field {

  // A sanitized whitelist of allowed map key types.
  private static final EnumSet VALID_KEY_TYPES =
    EnumSet.of(FieldType.INT32,
               FieldType.INT64,
               FieldType.UINT32,
               FieldType.UINT64,
               FieldType.SINT32,
               FieldType.SINT64,
               FieldType.FIXED32,
               FieldType.FIXED64,
               FieldType.SFIXED32,
               FieldType.SFIXED64,
               FieldType.BOOL,
               FieldType.STRING);

  /** Creates a builder for an {@link MapFieldSpec}. */
  public static Builder builder(FieldType keyType, FieldType valueType, String fieldName, int fieldNumber) {
    checkNotNull(keyType, "key field type may not be null");
    checkArgument(VALID_KEY_TYPES.contains(keyType),
                  "key type must be of an acceptable type (%s)",
                  VALID_KEY_TYPES);
    checkNotNull(valueType, "value field type may not be null");
    checkNotNull(fieldName, "field name may not be null");
    checkArgument(fieldNumber > 0, "field number must be positive");
    return new Builder(keyType, valueType, fieldName, fieldNumber);
  }

  private final FieldType keyType;
  private final FieldType valueType;
  private final String fieldName;
  private final int fieldNumber;
  private final ImmutableList<String> fieldComment;
  private final String customTypeName;
  private final ImmutableList<OptionSpec> options;

  private MapFieldSpec(Builder builder) {
    keyType = builder.keyType;
    valueType = builder.valueType;
    fieldName = builder.fieldName;
    fieldNumber = builder.fieldNumber;
    fieldComment = ImmutableList.copyOf(builder.fieldComment);
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
    String actualValueType = valueType.toString();
    if (valueType == FieldType.ENUM || valueType == FieldType.MESSAGE) {
      checkNotNull(customTypeName,
                   String.format("'%s' type is %s and requires a custom type name associated with it",
                                 fieldName, valueType));
      actualValueType = customTypeName;
    }
    writer.emit(String.format("map<%s, %s> %s = %d",
                              keyType, actualValueType, fieldName, fieldNumber));
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

  /** Builder for a {@link MapFieldSpec}. */
  public static final class Builder implements Buildable<MessageField> {

    private final FieldType keyType;
    private final FieldType valueType;
    private final String fieldName;
    private final int fieldNumber;
    private ImmutableList<String> fieldComment = ImmutableList.of();
    private String customTypeName = null;
    private ImmutableList<OptionSpec> options = ImmutableList.of();

    private Builder(FieldType keyType, FieldType valueType, String fieldName, int fieldNumber) {
      this.keyType = keyType;
      this.valueType = valueType;
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

    /** Declares a custom type name. For use when field is type is a Message or Enum. */
    public Builder setCustomTypeName(String customTypeName) {
      checkNotNull(customTypeName, "custom type name may not be null");
      checkState(valueType == FieldType.MESSAGE || valueType == FieldType.ENUM,
                 "custom type names only supported for MESSAGE and ENUM value types");
      this.customTypeName = customTypeName;
      return this;
    }

    /** Adds options to the field. See {@link OptionSpec}. */
    public Builder addFieldOptions(Iterable<Buildable<OptionSpec>> options) {
      this.options = ImmutableList.<OptionSpec>builder()
        .addAll(this.options)
        .addAll(Buildables.buildAll(options,
                                   opt -> checkArgument(opt.optionType() == OptionType.FIELD,
                                                        "option must be field type")))
        .build();
      return this;
    }

    /** Adds options to the field. See {@link OptionSpec}. */
    @SafeVarargs
    public final Builder addFieldOptions(Buildable<OptionSpec>... options) {
      return addFieldOptions(ImmutableList.copyOf(options));
    }

    /** Builds a new instance of {@link MapFieldSpec}. */
    @Override
    public MessageField build() {
      return new MapFieldSpec(this);
    }
  }
                                                                        
}
