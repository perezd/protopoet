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

import java.io.IOException;

/** 
 * Represents a valid protobuf value that may be assigned to a field. This was primarly
 * developed conceptually to support messages as values as part of {@link OptionSpec}.
 */
public final class FieldValue {

  /** Returns a {@link FieldValue} for an integer value. (eg: int32, uint32, fixed32, sfixed32). */
  public static FieldValue of(String fieldName, FieldType valueType, int intValue) {
    checkNotNull(fieldName, "field name may not be null");
    switch (valueType) {
    case INT32:
    case UINT32:
    case FIXED32:
    case SFIXED32:
      return new FieldValue(fieldName, valueType, intValue);
    default:
      throw new IllegalArgumentException(String.format("'%s' invalid type for an int value",
                                                       valueType));
    }
  }

  /** Returns a {@link FieldValue} for a long value. (eg: int64, uint64, fixed64, sfixed64). */
  public static FieldValue of(String fieldName, FieldType valueType, long longValue) {
    checkNotNull(fieldName, "field name may not be null");
    switch (valueType) {
    case INT64:
    case UINT64:
    case FIXED64:
    case SFIXED64:
      return new FieldValue(fieldName, valueType, longValue);
    default:
      throw new IllegalArgumentException(String.format("'%s' invalid type for a long value",
                                                       valueType));
    }
  }

  /** Returns a {@link FieldValue} for a float value. */
  public static FieldValue of(String fieldName, FieldType valueType, float floatValue) {
    checkNotNull(fieldName, "field name may not be null");
    checkArgument(valueType == FieldType.FLOAT,
                  String.format("'%s' invalid type for a float value",
                                valueType));
    return new FieldValue(fieldName, valueType, floatValue);
  }

  /** Returns a {@link FieldValue} for a double value. */
  public static FieldValue of(String fieldName, FieldType valueType, double doubleValue) {
    checkNotNull(fieldName, "field name may not be null");
    checkArgument(valueType == FieldType.DOUBLE,
                  String.format("'%s' invalid type for a double value",
                                valueType));
    return new FieldValue(fieldName, valueType, doubleValue);
  }

  /** Returns a {@link FieldValue} for a float value. */
  public static FieldValue of(String fieldName, FieldType valueType, String stringValue) {
    checkNotNull(fieldName, "field name may not be null");
    checkNotNull(stringValue, "value may not be null");
    switch (valueType) {
    case ENUM:
    case STRING:
    case BYTES:
      return new FieldValue(fieldName, valueType, stringValue);
    default:
      throw new IllegalArgumentException(String.format("'%s' invalid type for a string value",
                                                       valueType));
    }
  }

  public static FieldValue of(String fieldName, FieldType valueType, boolean boolValue) {
    checkNotNull(fieldName, "field name may not be null");
    checkArgument(valueType == FieldType.BOOL,
                  String.format("'%s' invalid type for a bool value",
                                valueType));
    return new FieldValue(fieldName, valueType, boolValue);
  }

  private final String fieldName;
  private final FieldType valueType;
  private final Object value;

  private FieldValue(String fieldName, FieldType valueType, Object value) {
    this.fieldName = fieldName;
    this.valueType = valueType;
    this.value = value;    
  }

  public String fieldName() {
    return fieldName;
  }

  public FieldType valueType() {
    return valueType;
  }

  public String formattedValue() {
    return valueType.formatValue(value);
  }
}
