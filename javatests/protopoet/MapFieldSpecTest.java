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
import org.junit.rules.ExpectedException;

public final class MapFieldSpecTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testMapKeyTypeValidationForMessage() {
    thrown.expect(IllegalArgumentException.class);
    MapFieldSpec.builder(FieldType.MESSAGE, FieldType.STRING, "a", 1);
  }

  @Test
  public void testMapKeyTypeValidationForEnum() {
    thrown.expect(IllegalArgumentException.class);
    MapFieldSpec.builder(FieldType.ENUM, FieldType.STRING, "a", 1);
  }

  @Test
  public void testBasicField() {
    output
      .expects("// comment\nmap<string, string> strmap = 1;\n")
      .produce(() ->
               MapFieldSpec.builder(FieldType.STRING, FieldType.STRING, "strmap", 1)
               .setFieldComment("comment")
               .build());
  }

  @Test
  public void testCustomTypeNameFieldMessage() {
    output
      .expects("// comment\nmap<string, Foo> test = 1;\n")
      .produce(() ->
               MapFieldSpec.builder(FieldType.STRING, FieldType.MESSAGE, "test", 1)
               .setFieldComment("comment")
               .setCustomTypeName("Foo")
               .build());
  }

  @Test
  public void testCustomTypeNameFieldEnum() {
    output
      .expects("// comment\nmap<string, Foo> test = 1;\n")
      .produce(() ->
               MapFieldSpec.builder(FieldType.STRING, FieldType.ENUM, "test", 1)
               .setFieldComment("comment")
               .setCustomTypeName("Foo")
               .build());
  }
 
  @Test
  public void testExceptionCustomTypeNameUsage() throws IOException {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("custom type names only supported for MESSAGE and ENUM value types");
    MapFieldSpec.builder(FieldType.STRING, FieldType.DOUBLE, "test", 1)
      .setCustomTypeName("Foo")
      .build()
      .emit(ProtoWriter.dud());    
  }


  @Test
  public void testWritingOptions() {
    output
      .expects("// comment\nmap<string, Foo> test = 1 [(foo) = true, (bar) = 23];\n")
      .produce(() ->
               MapFieldSpec.builder(FieldType.STRING, FieldType.ENUM, "test", 1)
               .setFieldComment("comment")
               .setCustomTypeName("Foo")
               .addFieldOptions(OptionSpec.builder(OptionType.FIELD, "foo").setValue(FieldType.BOOL, true),
                                OptionSpec.builder(OptionType.FIELD, "bar").setValue(FieldType.INT32, 23))
               .build());
  }
}
