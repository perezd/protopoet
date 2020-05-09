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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;

/**
 * Models a Proto file using proto3 syntax. Learn more:
 * https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#proto_file
 */
public final class ProtoFile implements Emittable {

  /** Creates a builder for a {@link ProtoFile}. */
  public static Builder builder() {
    return new Builder();
  }

  private final String packageName;
  private final ImmutableList<String> fileComment;
  private final ImmutableList<ImmutableList<Emittable>> srcBlocks;
  private final ImmutableList<ImportSpec> explicitImports;
  private final ImmutableList<OptionSpec> options;
  private final UsedNameMonitor usedNameMonitor = new UsedNameMonitor();

  private ProtoFile(Builder builder) {
    packageName = builder.packageName;
    fileComment = ImmutableList.copyOf(builder.fileComment);
    srcBlocks = ImmutableList.copyOf(builder.srcBlocks);
    explicitImports = ImmutableList.copyOf(builder.imports);
    options = ImmutableList.copyOf(builder.options);
  }

  /** Writes the file contents to the provided {@link Appendable}. */
  public void writeTo(Appendable out) throws IOException {
    emit(new ProtoWriter(out));
  }

  @Override
  public String toString() {
    try {
      StringBuilder result = new StringBuilder();
      writeTo(result);
      return result.toString();
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    return toString().equals(o.toString());
  }

  @Override
  public void emit(ProtoWriter writer) throws IOException {
    usedNameMonitor.reset();
    if (!fileComment.isEmpty()) {
      writer.emitComment(fileComment);
    }

    // ProtoPoet only supports proto3, so we hard code this.
    writer.emit("syntax = \"proto3\";").emit("\n");

    // Packages are optional.
    if (packageName != null) {
      writer.emit("\n").emit(String.format("package %s;", packageName)).emit("\n");
    }

    // Capture all unique implicit imports based on the provided src blocks.
    Iterable<ImportSpec> implicitImports =
        srcBlocks.stream()
            .flatMap(Collection::stream)
            .filter(b -> b instanceof Importable)
            .map(b -> ((Importable) b).imports())
            .flatMap(Streams::stream)
            .distinct()
            .collect(ImmutableList.toImmutableList());

    // Ensure that we have a sorted unique set of all imports to hoist
    // to the top of our file.
    ImmutableList<ImportSpec> uniqueSortedImports =
        ImmutableList.sortedCopyOf(
            Comparator.comparing(ImportSpec::path),
            ImmutableSet.<ImportSpec>builder()
                .addAll(explicitImports)
                .addAll(implicitImports)
                .build()
                .asList());

    // Hoist all imports to the top of the file.
    if (!uniqueSortedImports.isEmpty()) {
      writer.emit("\n");
      for (ImportSpec impt : uniqueSortedImports) {
        impt.emit(writer);
      }
    }

    // Next, hoist and render any file options that were provided.
    if (!options.isEmpty()) {
      writer.emit("\n");
      for (OptionSpec option : options) {
        option.emit(writer);
      }
    }

    // Finally, emit all emittables we've recorded in addition order.
    for (ImmutableList<Emittable> emittables : srcBlocks) {
      writer.emit("\n");
      for (Emittable emittable : emittables) {
        try {
          if (emittable instanceof Useable.Name) {
            usedNameMonitor.add((Useable.Name) emittable);
          }
          emittable.emit(writer);
        } catch (UsageException ex) {
          throw new IOException(ex);
        }
      }
    }
    writer.emit("\n");
  }

  /** Builder for producing new instances of {@link ProtoFile}. */
  public static final class Builder {

    private String packageName = null;
    private ImmutableList<String> fileComment = ImmutableList.of();
    private ImmutableList<ImportSpec> imports = ImmutableList.of();
    private ImmutableList<ImmutableList<Emittable>> srcBlocks = ImmutableList.of();
    private ImmutableList<OptionSpec> options = ImmutableList.of();

    private Builder() {}

    /** Declares a top-level file comment for the proto file. */
    public Builder setFileComment(Iterable<String> lines) {
      fileComment = ImmutableList.copyOf(lines);
      return this;
    }

    /** Declares a top-level file comment for the proto file. */
    public Builder setFileComment(String... lines) {
      return setFileComment(ImmutableList.copyOf(lines));
    }

    /** Declares a package name for the proto file. */
    public Builder setPackageName(String packageName) {
      this.packageName = checkNotNull(packageName, "package name cannot be null");
      return this;
    }

    /** Declares a list of imports for the proto file. See {@link ImportSpec}. */
    public Builder addImports(Iterable<? extends Buildable<ImportSpec>> impts) {
      imports =
          ImmutableList.<ImportSpec>builder()
              .addAll(imports)
              .addAll(ImmutableList.copyOf(Buildables.buildAll(impts)))
              .build();
      return this;
    }

    /** Declares a list of imports for the proto file. See {@link ImportSpec}. */
    @SafeVarargs
    public final Builder addImports(Buildable<ImportSpec>... impts) {
      return addImports(ImmutableList.copyOf(impts));
    }

    /** Declares a list of top level messages for the proto file. See {@link MessageSpec}. */
    public Builder addMessages(Iterable<? extends Buildable<MessageSpec>> msgs) {
      srcBlocks =
          ImmutableList.<ImmutableList<Emittable>>builder()
              .addAll(srcBlocks)
              .add(ImmutableList.copyOf(Buildables.buildAll(msgs)))
              .build();
      return this;
    }

    /** Declares a list of top level messages for the proto file. See {@link MessageSpec}. */
    public Builder addMessages(Buildable<MessageSpec>... msgs) {
      return addMessages(ImmutableList.copyOf(msgs));
    }

    /** Declares a list of top level eums for the proto file. See {@link EnumSpec}. */
    public Builder addEnums(Iterable<? extends Buildable<EnumSpec>> enums) {
      srcBlocks =
          ImmutableList.<ImmutableList<Emittable>>builder()
              .addAll(srcBlocks)
              .add(ImmutableList.copyOf(Buildables.buildAll(enums)))
              .build();
      return this;
    }

    /** Declares a list of top level eums for the proto file. See {@link EnumSpec}. */
    public Builder addEnums(Buildable<EnumSpec>... enums) {
      return addEnums(ImmutableList.copyOf(enums));
    }

    /** Declares a list of top level extensions for the proto file. See {@link ExtensionSpec}. */
    public Builder addExtensions(Iterable<? extends Buildable<ExtensionSpec>> extensions) {
      srcBlocks =
          ImmutableList.<ImmutableList<Emittable>>builder()
              .addAll(srcBlocks)
              .add(ImmutableList.copyOf(Buildables.buildAll(extensions)))
              .build();
      return this;
    }

    /** Declares a list of top level extensions for the proto file. See {@link ExtensionSpec}. */
    public Builder addExtensions(Buildable<ExtensionSpec>... extensions) {
      return addExtensions(ImmutableList.copyOf(extensions));
    }

    /** Declares a list of top level services for the proto file. See {@link ServiceSpec}. */
    public Builder addServices(Iterable<? extends Buildable<ServiceSpec>> services) {
      srcBlocks =
          ImmutableList.<ImmutableList<Emittable>>builder()
              .addAll(srcBlocks)
              .add(ImmutableList.copyOf(Buildables.buildAll(services)))
              .build();
      return this;
    }

    /** Declares a list of top level services for the proto file. See {@link ServiceSpec}. */
    public Builder addServices(Buildable<ServiceSpec>... services) {
      return addServices(ImmutableList.copyOf(services));
    }

    /** Adds options to the file. See {@link OptionSpec}. */
    public Builder addFileOptions(Iterable<? extends Buildable<OptionSpec>> options) {
      this.options =
          ImmutableList.<OptionSpec>builder()
              .addAll(this.options)
              .addAll(
                  Buildables.buildAll(
                      options,
                      opt ->
                          checkArgument(
                              opt.optionType() == OptionType.FILE, "option must be file type")))
              .build();
      return this;
    }

    /** Adds options to the file. See {@link OptionSpec}. */
    public Builder addFileOptions(Buildable<OptionSpec>... options) {
      return addFileOptions(ImmutableList.copyOf(options));
    }

    /** Generates an instance of {@link ProtoFile}. */
    public ProtoFile build() {
      return new ProtoFile(this);
    }
  }
}
