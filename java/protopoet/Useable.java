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

import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.Optional;

/** Designed to be used in conjunction with {@link UsedFieldMonitor} or {@link UsedNameMonitor}. */
interface Useable {

  /** Designates a field that can be used and should not be reused within a specific scope. */
  interface Field {

    /** Provides the name of a field. */
    String fieldName();

    /** 
     * Note, field numbers are not always required (RPC fields, for instance), but should
     * be made available whenever possible to ensure they are not accidentally reused.
     */
    Optional<Integer> fieldNumber();
  }

  /** Designates a block of reservations that should be considered used by the current scope. */
  interface FieldReservations {

    /** Provides a stream of distinct field numbers. */
    IntStream asFieldNumberStream();

    /** Provides a stream of discint field names. */
    Stream<String> asFieldNameStream();
  }

  /** Designates a contiguous block of fields. */
  interface Fields {

    /** Provides the name for the block of fields. */
    String fieldName();

    /** Provides a collection of fields. */
    Iterable<Field> fields();
  }

  /** Designates an entity that can have a consistently identifiable name */
  interface Name {
    /** Provides the name of an entity. */
    String name();
  }
}
