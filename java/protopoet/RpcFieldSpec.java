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
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Defines an RPC field for use with {@link ServiceSpec}.
 * Learn more: https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#service_definition
 */
public final class RpcFieldSpec implements Useable.Field, Emittable, Buildable<RpcFieldSpec> {

  /** Creates a builder for an {@link RpcFieldSpec}. */
  public static Builder builder(String fieldName) {
    checkNotNull(fieldName, "field name may not be null");
    return new Builder(fieldName);
  }

  private final String fieldName;
  private final ImmutableList<String> fieldComment;
  private final ImmutableMap<FieldPart, Map<String, Boolean>> fieldParts;
  private final ImmutableList<OptionSpec> options;
  
  private RpcFieldSpec(Builder builder) {
    fieldName = builder.fieldName;
    fieldComment = ImmutableList.copyOf(builder.fieldComment);
    fieldParts = ImmutableMap.copyOf(builder.fieldParts);
    options = ImmutableList.copyOf(builder.options);
  }

  @Override
  public void emit(ProtoWriter writer) throws IOException {
    if (!fieldComment.isEmpty()) {
      writer.emitComment(fieldComment);
    }    
    writer.emit(String.format("rpc %s (%s) returns (%s)",
                              fieldName,
                              formatFieldPart(FieldPart.REQUEST),
                              formatFieldPart(FieldPart.RESPONSE)));

    if (options.isEmpty()) {
      writer.emit(";\n");
      return;
    }
    writer.emit(" {\n").indent();
    for (OptionSpec option : options) {
      option.emit(writer);
    }
    writer
      .unindent()
      .emit("}\n");
  }

  @Override
  public RpcFieldSpec build() {
    return this;
  }

  @Override
  public String fieldName() {
    return fieldName;
  }

  @Override
  public Optional<Integer> fieldNumber() {
    return Optional.empty();
  }

  private String formatFieldPart(FieldPart fieldPart) {
    return fieldParts.get(fieldPart)
      .entrySet()
      .stream()
      .map(entry -> String.format("%s%s", entry.getValue() ? "stream " : "", entry.getKey()))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("expected a field part"));
  }

  /** Builder for producing new instances of {@link RpcFieldSpec}. */
  public static final class Builder implements Buildable<RpcFieldSpec> {

    private final String fieldName;
    private ImmutableMap<FieldPart, Map<String, Boolean>> fieldParts = ImmutableMap.of();
    private ImmutableList<String> fieldComment = ImmutableList.of();
    private ImmutableList<OptionSpec> options = ImmutableList.of();
    
    private Builder(String fieldName) {
      this.fieldName = fieldName;
    }

    /** Declares a comment for the field.*/
    public Builder setFieldComment(Iterable<String> lines) {
      this.fieldComment = ImmutableList.copyOf(lines);
      return this;
    }

    /** Declares a comment for the field.*/
    public Builder setFieldComment(String... lines) {
      return setFieldComment(ImmutableList.copyOf(lines));
    }

    /** Declares the message name for the request part of the field. */
    public Builder setRequestMessageName(String messageName) {
      return setRequestMessageName(messageName, false);
    }

    /**
     * Declares the message name for the request part of the field and 
     * signals if this part of the rpc is streaming.
     */
    public Builder setRequestMessageName(String messageName, boolean isStreaming) {
      fieldParts = newFieldParts(FieldPart.REQUEST, messageName, isStreaming);
      return this;
    }

    /** Declares the message name for the response part of the field. */
    public Builder setResponseMessageName(String messageName) {
      return setResponseMessageName(messageName, false);
    }

    /**
     * Declares the message name for the response part of the field and 
     * signals if this part of the rpc is streaming.
     */
    public Builder setResponseMessageName(String messageName, boolean isStreaming) {
      fieldParts = newFieldParts(FieldPart.RESPONSE, messageName, isStreaming);
      return this;
    }

    /** Adds options to the field. See {@link OptionSpec}. */
    public Builder addFieldOptions(Iterable<Buildable<OptionSpec>> options) {
      this.options = ImmutableList.<OptionSpec>builder()
        .addAll(this.options)
        .addAll(Buildables.buildAll(options,
                                   opt -> checkArgument(opt.optionType() == OptionType.METHOD,
                                                        "option must be method type")))
        .build();
      return this;
    }

    /** Adds options to the field. See {@link OptionSpec}. */
    @SafeVarargs
    public final Builder addFieldOptions(Buildable<OptionSpec>... options) {
      return addFieldOptions(ImmutableList.copyOf(options));
    }

    /** Builds a new instance of {@link RpcFieldSpec}. */
    @Override 
    public RpcFieldSpec build() {
      checkState(fieldParts.containsKey(FieldPart.REQUEST), "request message must be set");
      checkState(fieldParts.containsKey(FieldPart.RESPONSE), "response message must be set");
      return new RpcFieldSpec(this);
    }

    private ImmutableMap<FieldPart, Map<String, Boolean>> newFieldParts(FieldPart fieldPart,
                                                                        String messageName,
                                                                        boolean isStreaming) {
      Map<FieldPart, Map<String, Boolean>> newPart = Maps.newHashMap(fieldParts);
      newPart.put(fieldPart, ImmutableMap.of(messageName, isStreaming));
      return ImmutableMap.copyOf(newPart);
    }
  }

  // Identifiers for designating the two primary parts of an rpc field.
  private enum FieldPart {
    REQUEST, RESPONSE
  }
}
