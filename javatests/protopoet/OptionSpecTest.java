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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class OptionSpecTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  @Rule public final ExpectedOutput output = ExpectedOutput.none();

  @Test
  public void testWritingNonFieldScalarOption_integer() {
    output
        .expects("// comment\noption (foo) = 2147483647;\n")
        .produce(
            () ->
                OptionSpec.builder(OptionType.MESSAGE, "foo")
                    .setOptionComment("comment")
                    .setValue(FieldType.INT32, Integer.MAX_VALUE)
                    .build());
  }

  @Test
  public void testWritingFieldScalarOption_integer() {
    output
        .expects("(foo) = 2147483647")
        .produce(
            () ->
                OptionSpec.builder(OptionType.FIELD, "foo")
                    .setValue(FieldType.INT32, Integer.MAX_VALUE)
                    .build());
  }

  @Test
  public void testWritingNonFieldScalarOption_long() {
    output
        .expects("// comment\noption (foo) = 9223372036854775807;\n")
        .produce(
            () ->
                OptionSpec.builder(OptionType.MESSAGE, "foo")
                    .setOptionComment("comment")
                    .setValue(FieldType.INT64, Long.MAX_VALUE)
                    .build());
  }

  @Test
  public void testWritingFieldScalarOption_long() {
    output
        .expects("(foo) = 9223372036854775807")
        .produce(
            () ->
                OptionSpec.builder(OptionType.FIELD, "foo")
                    .setValue(FieldType.INT64, Long.MAX_VALUE)
                    .build());
  }

  @Test
  public void testWritingNonFieldScalarOption_float() {
    output
        .expects("// comment\noption (foo) = 3.4028235E38;\n")
        .produce(
            () ->
                OptionSpec.builder(OptionType.MESSAGE, "foo")
                    .setOptionComment("comment")
                    .setValue(FieldType.FLOAT, Float.MAX_VALUE)
                    .build());
  }

  @Test
  public void testWritingFieldScalarOption_float() {
    output
        .expects("(foo) = 3.4028235E38")
        .produce(
            () ->
                OptionSpec.builder(OptionType.FIELD, "foo")
                    .setValue(FieldType.FLOAT, Float.MAX_VALUE)
                    .build());
  }

  @Test
  public void testWritingNonFieldScalarOption_double() {
    output
        .expects("// comment\noption (foo) = 1.7976931348623157E308;\n")
        .produce(
            () ->
                OptionSpec.builder(OptionType.MESSAGE, "foo")
                    .setOptionComment("comment")
                    .setValue(FieldType.DOUBLE, Double.MAX_VALUE)
                    .build());
  }

  @Test
  public void testWritingFieldScalarOption_double() {
    output
        .expects("(foo) = 1.7976931348623157E308")
        .produce(
            () ->
                OptionSpec.builder(OptionType.FIELD, "foo")
                    .setValue(FieldType.DOUBLE, Double.MAX_VALUE)
                    .build());
  }

  @Test
  public void testWritingNonFieldScalarOption_enum() {
    output
        .expects("// comment\noption (foo) = FOO;\n")
        .produce(
            () ->
                OptionSpec.builder(OptionType.MESSAGE, "foo")
                    .setOptionComment("comment")
                    .setValue(FieldType.ENUM, "FOO")
                    .build());
  }

  @Test
  public void testWritingFieldScalarOption_enum() {
    output
        .expects("(foo) = FOO")
        .produce(
            () ->
                OptionSpec.builder(OptionType.FIELD, "foo")
                    .setValue(FieldType.ENUM, "FOO")
                    .build());
  }

  @Test
  public void testWritingNonFieldScalarOption_string() {
    output
        .expects("// comment\noption (foo) = \"hello\";\n")
        .produce(
            () ->
                OptionSpec.builder(OptionType.MESSAGE, "foo")
                    .setOptionComment("comment")
                    .setValue(FieldType.STRING, "hello")
                    .build());
  }

  @Test
  public void testWritinFieldScalarOption_string() {
    output
        .expects("(foo) = \"hello\"")
        .produce(
            () ->
                OptionSpec.builder(OptionType.FIELD, "foo")
                    .setValue(FieldType.STRING, "hello")
                    .build());
  }

  @Test
  public void testWritingNonFieldScalarOption_boolean() {
    output
        .expects("// comment\noption (foo) = true;\n")
        .produce(
            () ->
                OptionSpec.builder(OptionType.MESSAGE, "foo")
                    .setOptionComment("comment")
                    .setValue(FieldType.BOOL, true)
                    .build());
  }

  @Test
  public void testWritingFieldScalarOption_boolean() {
    output
        .expects("(foo) = true")
        .produce(
            () ->
                OptionSpec.builder(OptionType.FIELD, "foo").setValue(FieldType.BOOL, true).build());
  }

  @Test
  public void testWellKnownFieldFormatting() {
    output
        .expects("// comment\noption java_package = \"com.whatever\";\n")
        .produce(
            () ->
                OptionSpec.builder(OptionType.FILE, "java_package")
                    .setOptionComment("comment")
                    .setValue(FieldType.STRING, "com.whatever")
                    .build());
  }

  @Test
  public void testMessageFieldValuesScalars() {
    output
        .expectsTestData()
        .produce(
            () ->
                OptionSpec.builder(OptionType.METHOD, "foo")
                    .setOptionComment("comment")
                    .setValue(
                        FieldType.MESSAGE,
                        FieldValue.of("bar", FieldType.STRING, "hello"),
                        FieldValue.of("baz", FieldType.BOOL, true))
                    .build());
  }

  @Test
  public void testMessageFieldFieldValuesScalars() {
    output
        .expects("(foo) = { bar: \"hello\" baz: true }")
        .produce(
            () ->
                OptionSpec.builder(OptionType.ENUM_VALUE, "foo")
                    .setValue(
                        FieldType.MESSAGE,
                        FieldValue.of("bar", FieldType.STRING, "hello"),
                        FieldValue.of("baz", FieldType.BOOL, true))
                    .build());
  }

  @Test
  public void testMessageFieldNameParensExplicit() {
    output
        .expects("(foo) = { bar: \"hello\" baz: true }")
        .produce(
            () ->
                OptionSpec.builder(OptionType.ENUM_VALUE, "(foo)")
                    .setValue(
                        FieldType.MESSAGE,
                        FieldValue.of("bar", FieldType.STRING, "hello"),
                        FieldValue.of("baz", FieldType.BOOL, true))
                    .build());
  }
}
