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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class UsedNameMonitorTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private UsedNameMonitor monitor;
  
  @Before
  public void setUp() {
    monitor = new UsedNameMonitor("test");
  }

  @Test
  public void testNameUsedThrows() throws UsageException {
    thrown.expect(UsageException.class);
    thrown.expectMessage("'foo' name already used in 'test'");
    monitor.add(() -> "foo");
    monitor.ensureUnused(() -> "foo");
  }
  
  @Test
  public void testResetNameUsable() throws UsageException {
    monitor.add(() -> "foo");
    monitor.reset();
    monitor.ensureUnused(() -> "foo");
  }
}
