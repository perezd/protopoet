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
import java.util.Arrays;

/** 
 * This class largely inspired by JavaPoet's CodeWriter class:
 * https://github.com/square/javapoet/blob/master/src/main/java/com/squareup/javapoet/CodeWriter.java
 */
@SuppressWarnings("SameParameterValue")
final class ProtoWriter {

  /** Useful for tests where we don't care about output. */
  static ProtoWriter dud() {
    Appendable devNull = new Appendable() {
        @Override public Appendable append(char c) { return this; }
        @Override public Appendable append(CharSequence c) { return this; }
        @Override public Appendable append(CharSequence c, int s, int e) { return this; }
      };
    return new ProtoWriter(devNull, " ", 80, 2);
  }

  private final LineWrapper out;
  private final String indentValue;
  private final int indentSize;

  private int indentLevel;
  private boolean trailingNewline;
  private boolean comment;

  /**
   * When emitting a statement, this is the line of the statement currently being written. The first
   * line of a statement is indented normally and subsequent wrapped lines are double-indented. This
   * is -1 when the currently-written line isn't part of a statement.
   */
  private int statementLine = -1;
  
  ProtoWriter(Appendable out) {
    this(out, " ", 80, 2);
  }
  
  ProtoWriter(Appendable out, String indentValue, int lineLength, int indentSize) {
    this.out = new LineWrapper(out, indentValue, lineLength);
    this.indentValue = indentValue;
    this.indentSize = indentSize;
  }

  ProtoWriter indent() {
    return indent(indentSize);
  }

  ProtoWriter unindent() {
    return unindent(indentSize);
  }

  ProtoWriter indent(int levels) {
    checkArgument(levels > 0, "indent level must be greater than 0");
    this.indentLevel += levels;
    return this;
  }

  ProtoWriter unindent(int levels) {
    checkArgument(this.indentLevel - levels >= 0, "cannot unindent %s from %s", levels, indentLevel);
    this.indentLevel -= levels;
    return this;
  }

  ProtoWriter emitComment(String... lines) throws IOException {
    return emitComment(Arrays.asList(lines));
  }

  ProtoWriter emitComment(Iterable<String> lines) throws IOException {
    for (String line : lines) {
      this.trailingNewline = true; // Force the '//' prefix for the comment.
      this.comment = true;
      try {
        emit(line);
        emit("\n");
      } finally {
        this.comment = false;
      }
    }
    return this;    
  }
  
  ProtoWriter emit(String s) throws IOException {
    boolean first = true;
    for (String line : s.split("\n", -1)) {
      // Emit a newline character. Make sure blank lines in comments look good.
      if (!first) {
        if (this.comment && this.trailingNewline) {
          emitIndentation();
          out.append("//");
        }
        out.append("\n");
        this.trailingNewline = true;
        if (this.statementLine != -1) {
          if (this.statementLine == 0) {
            indent(2); // Begin multiple-line statement. Increase the indentation level.
          }
          this.statementLine++;
        }
      }

      first = false;
      if (line.isEmpty()) continue; // Don't indent empty lines.

      // Emit indentation and comment prefix if necessary.
      if (this.trailingNewline) {
        emitIndentation();
        if (this.comment) {
          out.append("// ");
        }
      }

      out.append(line);
      this.trailingNewline = false;
    }
    return this;
  }

  private void emitIndentation() throws IOException {
    for (int j = 0; j < indentLevel; j++) {
      out.append(indentValue);
    }
  }
}
