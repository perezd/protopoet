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

import java.io.IOException;
import java.util.Objects;

/**
 * Implements spec for the import statement of the Protocol Buffer language.
 * more info: https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#import_statement
 */
public final class ImportSpec implements Buildable<ImportSpec> {

  /** Defines import modifiers for use with an {@link ImportSpec}. */
  public enum Modifier {
    NONE(" "),
    WEAK(" weak "),
    PUBLIC(" public ");
      
    final String value;
      
    Modifier(String value) {
      this.value = value;
    }
  }

  /** Creates a builder for an {@link ImportSpec}. */
  public static ImportSpec of(String path) {
    return of(Modifier.NONE, path);
  }

  /** Creates a builder for an {@link ImportSpec}. */
  public static ImportSpec of(Modifier modifier, String path) {
    return new ImportSpec(modifier, path);
  }

  private final Modifier modifier;
  private final String path;
  
  private ImportSpec(Modifier modifier, String path) {
    checkArgument(path.endsWith(".proto"), "path must be a file ending with .proto");
    this.modifier = modifier;
    this.path = path;
  }

  public void emit(ProtoWriter writer) throws IOException {
    writer
      .emit(String.format("import%s\"%s\";", modifier.value, path))
      .emit("\n");
  }

  @Override
  public ImportSpec build() {
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(modifier, path);
  }

  @Override
  public boolean equals(Object obj) {
    // self check
    if (this == obj) {
      return true;
    }
    // null check
    if (obj == null) {
      return false;
    }
    // type check and cast
    if (getClass() != obj.getClass()) {
      return false;
    }
    // field comparison
    ImportSpec spec = (ImportSpec) obj;
    return Objects.equals(modifier, spec.modifier)
            && Objects.equals(path, spec.path);
  }

  String path() {
    return path;
  }
}
