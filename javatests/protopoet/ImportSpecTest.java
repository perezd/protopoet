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

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import protopoet.ImportSpec.Modifier;
  
public final class ImportSpecTest {

  private StringBuilder out;
  private ProtoWriter protoWriter;

  @Before
  public void setUp() {
    this.out = new StringBuilder();
    this.protoWriter = new ProtoWriter(out);
  }
  
  @Test
  public void writeBasicImport() throws IOException {
    ImportSpec.of("other.proto").emit(protoWriter);
    assertThat("import \"other.proto\";\n").isEqualTo(out.toString());
  }

  @Test
  public void writePublicImport() throws IOException {
    ImportSpec.of(Modifier.PUBLIC, "other.proto").emit(protoWriter);
    assertThat("import public \"other.proto\";\n").isEqualTo(out.toString());
  }

  @Test
  public void writeWeakImport() throws IOException {
    ImportSpec.of(Modifier.WEAK, "other.proto").emit(protoWriter);
    assertThat("import weak \"other.proto\";\n").isEqualTo(out.toString());
  }
}
