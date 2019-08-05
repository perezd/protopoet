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

/**
 * All standard scalar types a field may represent. Learn more:
 * https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#fields
 */
public enum FieldType {
  DOUBLE,
  FLOAT,
  INT32,
  INT64,
  UINT32,
  UINT64,
  SINT32,
  SINT64,
  FIXED32,
  FIXED64,
  SFIXED32,
  SFIXED64,
  BOOL,
  STRING,
  BYTES,
  MESSAGE,
  ENUM;

  /**
   * Attempts to guess at the FieldType based on the provided value. Note, this outputs INT32
   * forInteger and INT64 for Long, if you need more specific types for numerics, don't use this.
   */
  public static FieldType inferFrom(Object value) {
    if (value instanceof Double) {
      return DOUBLE;
    }
    if (value instanceof Float) {
      return FLOAT;
    }
    if (value instanceof Integer) {
      return INT32;
    }
    if (value instanceof Long) {
      return INT64;
    }
    if (value instanceof Boolean) {
      return BOOL;
    }
    if (value instanceof String) {
      return STRING;
    }
    if (value instanceof byte[]) {
      return BYTES;
    }
    if (value instanceof Enum) {
      return ENUM;
    }
    if (value instanceof Object) {
      return MESSAGE;
    }
    throw new IllegalArgumentException("unable to express FieldType for value");
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }

  String formatValue(Object value) {
    switch (this) {
      case STRING:
      case BYTES:
        return String.format("\"%s\"", value);
      default:
        return value.toString();
    }
  }
}
