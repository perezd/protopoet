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

import java.util.stream.IntStream;

/** Represents a range of field numbers. */
public final class FieldRange {

  /** Returns an immutable instance of a {@link FieldRange}. */
  public static FieldRange of(int lo, int hi) {
    checkArgument(lo > 0, "low number cannot be negative");
    checkArgument(hi > 0, "high number cannot be negative");
    checkArgument(hi > lo, "high value must be higher than low value");
    return new FieldRange(lo, hi);
  }

  private final int lo;
  private final int hi;

  private FieldRange(int lo, int hi) {
    this.lo = lo;
    this.hi = hi;
  }

  @Override
  public String toString() {
    return String.format("%d to %d", lo, hi);
  }

  IntStream asStream() {
    return IntStream.range(this.lo, this.hi + 1);
  }
}
