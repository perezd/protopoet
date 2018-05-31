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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import java.util.function.Consumer;

final class Buildables {

  // Helper method that reduces call site boilerplate when using a Buildable.
  static <T extends Buildable<T>> Iterable<T> buildAll(Iterable<Buildable<T>> buildables) {
    return Streams.stream(buildables)
      .map(Buildable::build)
      .collect(ImmutableList.toImmutableList());
  }

  // Helper method that reduces call site boilerplate when using a Buildable. Allows a peekable.
  static <T extends Buildable<T>> Iterable<T> buildAll(Iterable<Buildable<T>> buildables, Consumer<T> checker) {
    return Streams.stream(buildables)
      .map(Buildable::build)
      .peek(checker)
      .collect(ImmutableList.toImmutableList());
  }
}
