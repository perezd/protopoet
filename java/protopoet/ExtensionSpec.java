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
 * Models an Extension using proto3 syntax.
 * <p>NOTE: Extensions are not an officially supported concept in proto3,
 * but they are allowed to support the concept of Custom Options.
 * Learn more here: https://developers.google.com/protocol-buffers/docs/proto3#custom_options
 */
public final class ExtensionSpec implements Emittable, Buildable<ExtensionSpec>, Importable, Useable.Name {

  /** Only building custom options is supported with {@link ExtensionSpec}. See {@link OptionType}. */
  public static Builder builder(OptionType optionType) {
    checkNotNull(optionType, "option type may not be null");
    return new Builder(optionType.optionClassName,
                       ImmutableList.of(ImportSpec.of("google/protobuf/descriptor.proto")));
  }

  private final String extensionName;
  private final ImmutableList<ImportSpec> implicitImports;
  private final ImmutableList<String> extensionComment;
  private final ImmutableList<MessageField> extensionFields;
  private final UsedFieldMonitor usedFieldMonitor = new UsedFieldMonitor();

  private ExtensionSpec(Builder builder) {
    extensionName = builder.extensionName;
    implicitImports = ImmutableList.copyOf(builder.implicitImports);
    extensionComment = ImmutableList.copyOf(builder.extensionComment);
    extensionFields = ImmutableList.copyOf(builder.extensionFields);
  }

  @Override
  public void emit(ProtoWriter writer) throws IOException {
    usedFieldMonitor.reset();
    if (!extensionComment.isEmpty()) {
      writer.emitComment(extensionComment);
    }

    // Writes all the provided message fields into the body of the extension,
    // tracking the used fields to prevent duplicate field names or numbers.
    writer.emit(String.format("extend %s {", extensionName));
    if (!extensionFields.isEmpty()) {
      writer
        .emit("\n")
        .indent();
      for (MessageField extensionField : extensionFields) {
        try {
          // Verify regular fields.
          if (extensionField instanceof Useable.Field) {
            usedFieldMonitor.add((Useable.Field) extensionField);
          }
          extensionField.emit(writer);
        } catch (UsageException ex) {
          throw new IOException(ex);
        }
      }
      writer.unindent();
    }
    writer.emit("}\n");
  }

  @Override
  public Iterable<ImportSpec> imports() {
    return implicitImports;
  }

  @Override
  public ExtensionSpec build() {
    return this;
  }

  @Override
  public String name() {
    return extensionName;
  }

  /** Builds a new instance of {@link ExtensionSpec}. */
  public static final class Builder implements Buildable<ExtensionSpec> {

    private final String extensionName;
    private final ImmutableList<ImportSpec> implicitImports;
    private ImmutableList<String> extensionComment = ImmutableList.of();
    private ImmutableList<MessageField> extensionFields = ImmutableList.of();

    private Builder(String extensionName, ImmutableList<ImportSpec> implicitImports) {
      this.extensionName = extensionName;
      this.implicitImports = implicitImports;
    }

    /** Declares a top-level comment for the extension. */
    public Builder setExtensionComment(Iterable<String> lines) {
      extensionComment = ImmutableList.copyOf(lines);
      return this;
    }

    /** Declares a top-level comment for the extension. */
    public Builder setExtensionComment(String... lines) {
      return setExtensionComment(ImmutableList.copyOf(lines));
    }

    /** 
     * Adds fields to the extension. See {@link MessageFieldSpec}. 
     * NOTE: oneofs and maps are not allowed as extension fields.
     */
    public Builder addExtensionFields(Iterable<? extends Buildable<MessageField>> fields) {
      extensionFields = ImmutableList.<MessageField>builder()
        .addAll(extensionFields)
        .addAll(Buildables.buildAll(fields, field ->
                                   checkArgument(field instanceof MessageFieldSpec,
                                                 "complex fields not allowed (eg: oneofs or maps)")))
        .build();
      return this;
    }

    /** 
     * Adds fields to the extension. See {@link MessageFieldSpec}. 
     * NOTE: oneofs and maps are not allowed as extension fields.
     */
    @SafeVarargs
    public final Builder addExtensionFields(Buildable<MessageField>... fields) {
      return addExtensionFields(ImmutableList.copyOf(fields));
    }

    /** Builds a new instance of {@link ExtensionSpec}. */
    @Override
    public ExtensionSpec build() {
      return new ExtensionSpec(this);
    }
  }
}
