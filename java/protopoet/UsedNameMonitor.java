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

import java.util.HashSet;
import java.util.function.Function;

/**
 * Monitor class that helps language emitters keep track of used names. Checkout {@link Useable}
 * interface for particular APIs that need to be configured to make use of this.
 */
final class UsedNameMonitor {

  private final Function<String, String> exceptionText;
  private final HashSet<String> names = new HashSet<>();

  UsedNameMonitor(String usageContext) {
    exceptionText =
        (candidateName) ->
            String.format("'%s' name already used in '%s'", candidateName, usageContext);
  }

  UsedNameMonitor() {
    exceptionText = (candidateName) -> String.format("'%s' name already used", candidateName);
  }

  /** Adds a name that has been used to prevent future usages. */
  void add(Useable.Name name) throws UsageException {
    ensureUnused(name);
    names.add(name.name());
  }

  /** Clears all known state for the monitor. */
  void reset() {
    names.clear();
  }

  /**
   * Verifies that a name hasn't be used before with this instance of the monitor. Throws {@link
   * UsageException} iif that is not true.
   */
  void ensureUnused(Useable.Name name) throws UsageException {
    String candidateName = name.name();
    if (names.contains(candidateName)) {
      throw new UsageException(exceptionText.apply(candidateName));
    }
  }
}
