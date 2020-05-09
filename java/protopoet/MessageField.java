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
 * Message fields are unique because they can be standard fields, oneof fields, or maps. This is a
 * type system hack to allow them to all polymorphically work seamlessly together in a single
 * statement. See {@link MessageFieldSpec}, {@link OneofFieldSpec}, and {@link MapFieldSpec}.
 */
public interface MessageField extends Emittable, Buildable<MessageField> {
  /** Provides the defined name for a message field. */
  String fieldName();
}
