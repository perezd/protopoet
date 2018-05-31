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

import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;

public final class EnumFieldSpecTest {
  
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testWritingField() {
    output
      .expects("// hello there\nFOO = 1;\n")
      .produce(() ->
               EnumFieldSpec.builder("FOO", 1)
               .setFieldComment("hello there")
               .build());
  }

  @Test
  public void testFieldOptions() {
    output
      .expects("// comment\nFOO = 1 [(foo) = true, (bar) = 56];\n")
      .produce(() ->
               EnumFieldSpec.builder("FOO", 1)
               .setFieldComment("comment")
               .addFieldOptions(OptionSpec.builder(OptionType.ENUM_VALUE, "foo")
                                .setValue(FieldType.BOOL, true),
                                OptionSpec.builder(OptionType.ENUM_VALUE, "bar")
                                .setValue(FieldType.INT32, 56))
               .build());
  }
}
