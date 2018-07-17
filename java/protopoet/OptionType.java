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
 * Valid types of options available in the Protocol Buffers language. 
 * Learn more: https://developers.google.com/protocol-buffers/docs/proto#customoptions
 */
enum OptionType {
  /** See {@link ProtoFile} for more info. */
  FILE("FileOptions"),

  /** See {@link MessageSpec} for more info. */
  MESSAGE("MessageOptions"),

  /** See {@link MessageFieldSpec} or {@link MapFieldSpec} for more info. */
  FIELD("FieldOptions"),

  /** See {@link EnumSpec} for more info. */
  ENUM("EnumOptions"),

  /** See {@link EnumFieldSpec} for more info. */
  ENUM_VALUE("EnumValueOptions"),

  /** See {@link ServiceSpec} for more info. */
  SERVICE("ServiceOptions"),

  /** See {@link OneofFieldSpec} for more info. */
  ONEOF("OneofOptions"),

  /** See {@link RpcFieldSpec} for more info. */
  METHOD("MethodOptions");

  final String optionClassName;

  OptionType(String messageName) {
    this.optionClassName = "google.protobuf." + messageName;
  }
}
