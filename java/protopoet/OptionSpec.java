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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;

/**
 * Models an Option in the Protocol Buffers language. Learn more:
 * https://developers.google.com/protocol-buffers/docs/proto#options
 */
public final class OptionSpec implements Buildable<OptionSpec>, Emittable {

  /** Creates a builder for a File {@link OptionSpec}. */
  public static Builder fileOption(String optionName) {
    return builder(OptionType.FILE, optionName);
  }

  /** Creates a builder for a Message {@link OptionSpec}. */
  public static Builder messageOption(String optionName) {
    return builder(OptionType.MESSAGE, optionName);
  }

  /** Creates a builder for a Field {@link OptionSpec}. */
  public static Builder fieldOption(String optionName) {
    return builder(OptionType.FIELD, optionName);
  }

  /** Creates a builder for a Enum {@link OptionSpec}. */
  public static Builder enumOption(String optionName) {
    return builder(OptionType.ENUM, optionName);
  }

  /** Creates a builder for a Enum Value {@link OptionSpec}. */
  public static Builder enumValueOption(String optionName) {
    return builder(OptionType.ENUM_VALUE, optionName);
  }

  /** Creates a builder for a Service {@link OptionSpec}. */
  public static Builder serviceOption(String optionName) {
    return builder(OptionType.SERVICE, optionName);
  }

  /** Creates a builder for a Oneof {@link OptionSpec}. */
  public static Builder oneofOption(String optionName) {
    return builder(OptionType.ONEOF, optionName);
  }

  /** Creates a builder for a Method {@link OptionSpec}. */
  public static Builder methodOption(String optionName) {
    return builder(OptionType.METHOD, optionName);
  }

  /** Creates a builder for an {@link OptionSpec}. */
  static Builder builder(OptionType optionType, String optionName) {
    checkNotNull(optionType, "option type may not be null");
    checkNotNull(optionName, "option name may not be null");
    return new Builder(optionType, optionName);
  }

  // Snippet of resuable logic for rendering field-based options.
  static ProtoWriter emitFieldOptions(List<OptionSpec> options, ProtoWriter writer)
      throws IOException {
    if (!options.isEmpty()) {
      writer.emit(" [");
      for (int i = 0; i < options.size(); i++) {
        options.get(i).emit(writer);
        if (i + 1 < options.size()) {
          writer.emit(", ");
        }
      }
      writer.emit("]");
    }
    return writer;
  }

  // Field option types have specialized formatting that are inconsistent with non-field options
  // such as Message, Service, File, etc.
  private static ImmutableSet<OptionType> FIELD_OPTION_TYPES =
      ImmutableSet.of(OptionType.FIELD, OptionType.ENUM_VALUE);

  // Protobufs have some well known option names that require special formatting to
  // disambiguate from custom options. This is largely a rendering implementation
  // detail so we keep a list sycned with this file:
  // https://github.com/google/protobuf/blob/master/src/google/protobuf/descriptor.proto
  private static ImmutableMap<OptionType, ImmutableSet<String>> WELL_KNOWN_OPTIONS;

  static {
    WELL_KNOWN_OPTIONS =
        ImmutableMap.<OptionType, ImmutableSet<String>>builder()
            .put(
                OptionType.FILE,
                ImmutableSet.of(
                    "java_package",
                    "java_outer_classname",
                    "java_multiple_files",
                    "java_generate_equals_and_hash",
                    "java_string_check_utf8",
                    "optimize_for",
                    "go_package",
                    "cc_generic_services",
                    "java_generic_services",
                    "py_generic_services",
                    "php_generic_services",
                    "deprecated",
                    "cc_enable_arenas",
                    "objc_class_prefix",
                    "csharp_namespace",
                    "swift_prefix",
                    "php_class_prefix",
                    "php_namespace"))
            .put(
                OptionType.MESSAGE,
                ImmutableSet.of(
                    "message_set_wire_format", "no_standard_descriptor_accessor", "deprecated"))
            .put(OptionType.SERVICE, ImmutableSet.of("deprecated"))
            .put(OptionType.ENUM, ImmutableSet.of("allow_alias", "deprecated"))
            .put(OptionType.ONEOF, ImmutableSet.of())
            .put(
                OptionType.FIELD,
                ImmutableSet.of("ctype", "packed", "jstype", "lazy", "deprecated", "weak"))
            .put(OptionType.ENUM_VALUE, ImmutableSet.of("deprecated"))
            .put(OptionType.METHOD, ImmutableSet.of("deprecated", "idempotency_level"))
            .build();
  }

  private final OptionType optionType;
  private final String optionName;
  private final ImmutableList<String> optionComment;
  private final FieldType optionValueType;
  private final Object optionValue;
  private final boolean isFieldOptionType;

  private OptionSpec(Builder builder) {
    optionType = builder.optionType;
    optionName = builder.optionName;
    optionComment = ImmutableList.copyOf(builder.optionComment);
    optionValueType = builder.optionValueType;
    optionValue = builder.optionValue;
    isFieldOptionType = FIELD_OPTION_TYPES.contains(optionType);
  }

  @Override
  public void emit(ProtoWriter writer) throws IOException {
    // Only emit comments if we're not being used in a field context,
    // and we have something to say.
    if (!isFieldOptionType && !optionComment.isEmpty()) {
      writer.emitComment(optionComment);
    }
    String formattedOptionName = formatOptionName(optionType, optionName);

    // Simple scalar rendering, can be done inline pretty simply for
    // both messsage and field use cases.
    if (optionValueType != FieldType.MESSAGE) {
      String formattedValue = optionValueType.formatValue(optionValue);
      String optionVariant = isFieldOptionType ? "%s = %s" : "option %s = %s;\n";
      writer.emit(String.format(optionVariant, formattedOptionName, formattedValue));
      return;
    }

    // Message value option types are formatted very differently, and require a completely
    // different approach for both field and non-field use cases.
    if (optionValueType == FieldType.MESSAGE) {
      if (!isFieldOptionType) {
        writer.emit(String.format("option %s = {\n", formattedOptionName)).indent();
        // Cast is safe here because the builder below enforces type safety.
        for (FieldValue fieldValue : (Iterable<FieldValue>) optionValue) {
          writer.emit(
              String.format("%s: %s\n", fieldValue.fieldName(), fieldValue.formattedValue()));
        }
        writer.unindent().emit("};\n");
        return;
      }

      // Lastly, if we're in a message value type but we're in option rendering mode.
      writer.emit(String.format("%s = { ", formattedOptionName));
      for (FieldValue fieldValue : (Iterable<FieldValue>) optionValue) {
        writer.emit(String.format("%s: %s ", fieldValue.fieldName(), fieldValue.formattedValue()));
      }
      writer.emit("}");
    }
  }

  @Override
  public OptionSpec build() {
    return this;
  }

  OptionType optionType() {
    return optionType;
  }

  private static String formatOptionName(OptionType optionType, String optionName) {
    checkState(
        WELL_KNOWN_OPTIONS.containsKey(optionType),
        String.format("unexpected option type: %s", optionType));
    return WELL_KNOWN_OPTIONS.get(optionType).contains(optionName) || optionName.startsWith("(")
        ? optionName
        : "(" + optionName + ")";
  }

  /** Builder for producing new instances of {@link OptionSpec}. */
  public static final class Builder implements Buildable<OptionSpec> {

    private final OptionType optionType;
    private final String optionName;
    private ImmutableList<String> optionComment = ImmutableList.of();
    private FieldType optionValueType;
    private Object optionValue;

    private Builder(OptionType optionType, String optionName) {
      this.optionType = optionType;
      this.optionName = optionName;
    }

    /**
     * Declares a top-level comment for the option. Note, this only renders for non-field options.
     */
    public Builder setOptionComment(Iterable<String> lines) {
      checkState(
          !OptionSpec.FIELD_OPTION_TYPES.contains(optionType),
          "comments aren't available for field options");
      optionComment = ImmutableList.copyOf(lines);
      return this;
    }

    /**
     * Declares a top-level comment for the option. Note, this only renders for non-field options.
     */
    public Builder setOptionComment(String... lines) {
      return setOptionComment(ImmutableList.copyOf(lines));
    }

    /** Sets an integer-based value (eg: int32, uint32, fixed32, sfixed32). */
    public Builder setValue(FieldType valueType, int intValue) {
      switch (valueType) {
        case INT32:
        case UINT32:
        case FIXED32:
        case SFIXED32:
          optionValueType = valueType;
          optionValue = intValue;
          return this;
        default:
          throw new IllegalArgumentException(
              String.format("'%s' invalid type for an int value", valueType));
      }
    }

    /** Sets a long-based value (eg: int64, uint64, fixed64, sfixed64). */
    public Builder setValue(FieldType valueType, long longValue) {
      switch (valueType) {
        case INT64:
        case UINT64:
        case FIXED64:
        case SFIXED64:
          optionValueType = valueType;
          optionValue = longValue;
          return this;
        default:
          throw new IllegalArgumentException(
              String.format("'%s' invalid type for a long value", valueType));
      }
    }

    /** Sets a float-based value. */
    public Builder setValue(FieldType valueType, float floatValue) {
      checkArgument(
          valueType == FieldType.FLOAT,
          String.format("'%s' invalid type for a float value", valueType));
      optionValueType = valueType;
      optionValue = floatValue;
      return this;
    }

    /** Sets a double-based value. */
    public Builder setValue(FieldType valueType, double doubleValue) {
      checkArgument(
          valueType == FieldType.DOUBLE,
          String.format("'%s' invalid type for a double value", valueType));
      optionValueType = valueType;
      optionValue = doubleValue;
      return this;
    }

    /** Sets a string-like value. (eg: enum, string, bytes). */
    public Builder setValue(FieldType valueType, String stringValue) {
      checkNotNull(stringValue, "value must not be null");
      switch (valueType) {
        case ENUM:
        case STRING:
        case BYTES:
          optionValueType = valueType;
          optionValue = stringValue;
          return this;
        default:
          throw new IllegalArgumentException(
              String.format("'%s' invalid type for a string value", valueType));
      }
    }

    /** Sets a boolean value. */
    public Builder setValue(FieldType valueType, boolean boolValue) {
      checkArgument(
          valueType == FieldType.BOOL,
          String.format("'%s' invalid type for a bool value", valueType));
      optionValueType = valueType;
      optionValue = boolValue;
      return this;
    }

    public Builder setValue(FieldType valueType, Iterable<FieldValue> fieldValues) {
      checkArgument(
          valueType == FieldType.MESSAGE,
          String.format("'%s' invalid type for a message value", valueType));
      optionValueType = valueType;
      optionValue = fieldValues;
      return this;
    }

    public Builder setValue(FieldType valueType, FieldValue... fieldValues) {
      return setValue(valueType, ImmutableList.copyOf(fieldValues));
    }

    /** Builds a new instance of {@link OptionSpec}. */
    @Override
    public OptionSpec build() {
      return new OptionSpec(this);
    }
  }
}
