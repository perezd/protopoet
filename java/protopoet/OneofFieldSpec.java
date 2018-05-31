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
import java.util.stream.Collectors;

public final class OneofFieldSpec implements MessageField, Useable.Fields {

  /** Creates a builder for a {@link OneofFieldSpec}. */
  public static Builder builder(String fieldName) {
    checkNotNull(fieldName, "field name may not be null");
    return new Builder(fieldName);
  }

  private final String fieldName;
  private final ImmutableList<String> fieldComment;
  private final ImmutableList<MessageField> fields;
  private final ImmutableList<OptionSpec> options;

  private OneofFieldSpec(Builder builder) {
    fieldName = builder.fieldName;
    fieldComment = ImmutableList.copyOf(builder.fieldComment);
    fields = ImmutableList.copyOf(builder.fields);
    options = ImmutableList.copyOf(builder.options);
  }
  
  @Override
  public void emit(ProtoWriter writer) throws IOException {
    if (!fieldComment.isEmpty()) {
      writer.emitComment(fieldComment);
    }
    writer.emit(String.format("oneof %s {", fieldName));

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

    // Finally, emit all provided fields.
    // Note that, we don't use a field monitor here, instead,
    // we propagate/delegate that to the parent entity.
    if (!fields.isEmpty()) {
      writer
        .emit("\n")
        .indent();
      for (MessageField field : fields) {
        field.emit(writer);
      }
      writer.unindent();
    }
    writer.emit("}\n");
  }

  @Override
  public String fieldName() {
    return fieldName;
  }

  @Override
  public Iterable<Useable.Field> fields() {
    // By convention, we only allow MessageFieldSpec and MapFieldSpec
    // which are both by definition useable in the scope this field
    // may be used within, so its safe to assume we can propagate them
    // to a used field monitor without worry.
    return fields.stream().map(f -> (Useable.Field) f).collect(Collectors.toList());
  }

  @Override
  public MessageField build() {
    return this;
  }

  /** Builder for a {@link OneofFieldSpec}. */
  public static final class Builder implements Buildable<MessageField> {

    private final String fieldName;
    private ImmutableList<String> fieldComment = ImmutableList.of();
    private ImmutableList<MessageField> fields = ImmutableList.of();
    private ImmutableList<OptionSpec> options = ImmutableList.of();

    private Builder(String fieldName) {
      this.fieldName = fieldName;
    }

    /** Declares a top-level comment for the field. */
    public Builder setFieldComment(Iterable<String> lines) {
      this.fieldComment = ImmutableList.copyOf(lines);
      return this;
    }

    /** Declares a top-level comment for the field. */
    public Builder setFieldComment(String... lines) {
      return setFieldComment(ImmutableList.copyOf(lines));
    }

    /** Adds fields to the oneof. See {@link MessageFieldSpec}. Maps and oneofs are not supported. */
    public Builder addMessageFields(Iterable<Buildable<MessageField>> fields) {
      Iterable<MessageField> oneofFields = Buildables.buildAll(fields);
      for (MessageField field : oneofFields) {
        // Spec says map fields are now allowed, specifically.
        checkState(!(field instanceof MapFieldSpec), "map field '%s' not allowed in oneof", field.fieldName());
        // Verify a recursive oneof isn't happening.
        checkState(!(field instanceof OneofFieldSpec),
                   String.format("immediate inner oneof field '%s' disallowed", field.fieldName()));

        // Spec says repeated fields are not allowed, specifically.
        if (field instanceof MessageFieldSpec) {
          MessageFieldSpec msgField = (MessageFieldSpec) field;
          checkState(!msgField.isRepeated(),
                     String.format("repeated field '%s' not allowed in oneof", msgField.fieldName()));
        }
      }

      this.fields = ImmutableList.<MessageField>builder()
        .addAll(this.fields)
        .addAll(oneofFields)
        .build();
      return this;
    }

    @SafeVarargs
    public final Builder addMessageFields(Buildable<MessageField>... fields) {
      return addMessageFields(ImmutableList.copyOf(fields));
    }

    /** Adds options to the oneof. See {@link OptionSpec}. */
    public Builder addOneofOptions(Iterable<Buildable<OptionSpec>> options) {
      this.options = ImmutableList.<OptionSpec>builder()
        .addAll(this.options)
        .addAll(Buildables.buildAll(options,
                                   opt -> checkArgument(opt.optionType() == OptionType.ONEOF,
                                                        "option must be oneof type")))
        .build();
      return this;
    }

    /** Adds options to the oneof. See {@link OptionSpec}. */
    public Builder addOneofOptions(Buildable<OptionSpec>... options) {
      return addOneofOptions(ImmutableList.copyOf(options));
    }

    @Override
    public MessageField build() {
      return new OneofFieldSpec(this);
    }
  }
}
