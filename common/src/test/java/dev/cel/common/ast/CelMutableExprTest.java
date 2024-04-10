// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package dev.cel.common.ast;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.testing.EqualsTester;
import com.google.testing.junit.testparameterinjector.TestParameter;
import com.google.testing.junit.testparameterinjector.TestParameterInjector;
import dev.cel.common.ast.CelExpr.ExprKind.Kind;
import dev.cel.common.ast.CelMutableExpr.CelMutableIdent;
import dev.cel.common.ast.CelMutableExpr.CelMutableSelect;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TestParameterInjector.class)
public class CelMutableExprTest {

  @Test
  public void ofNotSet() {
    CelMutableExpr mutableExpr = CelMutableExpr.ofNotSet();

    assertThat(mutableExpr.id()).isEqualTo(0L);
    assertThat(mutableExpr.notSet()).isNotNull();
  }

  @Test
  public void ofNotSet_withId() {
    CelMutableExpr mutableExpr = CelMutableExpr.ofNotSet(1L);

    assertThat(mutableExpr.id()).isEqualTo(1L);
    assertThat(mutableExpr.notSet()).isNotNull();
  }

  @Test
  public void ofConstant() {
    CelMutableExpr mutableExpr = CelMutableExpr.ofConstant(CelConstant.ofValue(5L));

    assertThat(mutableExpr.id()).isEqualTo(0L);
    assertThat(mutableExpr.constant()).isEqualTo(CelConstant.ofValue(5L));
  }

  @Test
  public void ofConstant_withId() {
    CelMutableExpr mutableExpr = CelMutableExpr.ofConstant(1L, CelConstant.ofValue(5L));

    assertThat(mutableExpr.id()).isEqualTo(1L);
    assertThat(mutableExpr.constant()).isEqualTo(CelConstant.ofValue(5L));
  }

  @Test
  public void ofIdent() {
    CelMutableExpr mutableExpr = CelMutableExpr.ofIdent("x");

    assertThat(mutableExpr.id()).isEqualTo(0L);
    assertThat(mutableExpr.ident().name()).isEqualTo("x");
  }

  @Test
  public void ofIdent_withId() {
    CelMutableExpr mutableExpr = CelMutableExpr.ofIdent(1L, "x");

    assertThat(mutableExpr.id()).isEqualTo(1L);
    assertThat(mutableExpr.ident().name()).isEqualTo("x");
  }

  @Test
  public void mutableIdent_setName() {
    CelMutableIdent ident = CelMutableIdent.create("x");

    ident.setName("y");

    assertThat(ident.name()).isEqualTo("y");
  }

  @Test
  public void ofSelect() {
    CelMutableExpr mutableExpr =
        CelMutableExpr.ofSelect(CelMutableSelect.create(CelMutableExpr.ofIdent("x"), "field"));

    assertThat(mutableExpr.id()).isEqualTo(0L);
    assertThat(mutableExpr.select().testOnly()).isFalse();
    assertThat(mutableExpr.select().field()).isEqualTo("field");
    assertThat(mutableExpr.select().operand()).isEqualTo(CelMutableExpr.ofIdent("x"));
  }

  @Test
  public void ofSelect_withId() {
    CelMutableExpr mutableExpr =
        CelMutableExpr.ofSelect(
            1L,
            CelMutableSelect.create(CelMutableExpr.ofIdent("x"), "field", /* testOnly= */ true));

    assertThat(mutableExpr.id()).isEqualTo(1L);
    assertThat(mutableExpr.select().testOnly()).isTrue();
    assertThat(mutableExpr.select().field()).isEqualTo("field");
    assertThat(mutableExpr.select().operand()).isEqualTo(CelMutableExpr.ofIdent("x"));
  }

  @Test
  public void mutableSelect_setters() {
    CelMutableSelect select =
        CelMutableSelect.create(CelMutableExpr.ofIdent("x"), "field", /* testOnly= */ true);

    select.setOperand(CelMutableExpr.ofConstant(CelConstant.ofValue(1L)));
    select.setField("field2");
    select.setTestOnly(false);

    assertThat(select.operand()).isEqualTo(CelMutableExpr.ofConstant(CelConstant.ofValue(1L)));
    assertThat(select.field()).isEqualTo("field2");
    assertThat(select.testOnly()).isFalse();
  }

  @Test
  public void setId_success() {
    CelMutableExpr mutableExpr = CelMutableExpr.ofConstant(CelConstant.ofValue(5L));

    mutableExpr.setId(2L);

    assertThat(mutableExpr.id()).isEqualTo(2L);
  }

  @Test
  public void equalityTest() {
    new EqualsTester()
        .addEqualityGroup(CelMutableExpr.ofNotSet())
        .addEqualityGroup(CelMutableExpr.ofNotSet(1L), CelMutableExpr.ofNotSet(1L))
        .addEqualityGroup(CelMutableExpr.ofConstant(1L, CelConstant.ofValue(2L)))
        .addEqualityGroup(
            CelMutableExpr.ofConstant(5L, CelConstant.ofValue("hello")),
            CelMutableExpr.ofConstant(5L, CelConstant.ofValue("hello")))
        .addEqualityGroup(CelMutableExpr.ofIdent("x"))
        .addEqualityGroup(CelMutableExpr.ofIdent(2L, "y"), CelMutableExpr.ofIdent(2L, "y"))
        .addEqualityGroup(
            CelMutableExpr.ofSelect(CelMutableSelect.create(CelMutableExpr.ofIdent("y"), "field")))
        .addEqualityGroup(
            CelMutableExpr.ofSelect(
                4L, CelMutableSelect.create(CelMutableExpr.ofIdent("x"), "test")),
            CelMutableExpr.ofSelect(
                4L, CelMutableSelect.create(CelMutableExpr.ofIdent("x"), "test")))
        .testEquals();
  }

  @SuppressWarnings("Immutable") // Mutable by design
  private enum MutableExprKindTestCase {
    NOT_SET(CelMutableExpr.ofNotSet(1L)),
    CONSTANT(CelMutableExpr.ofConstant(CelConstant.ofValue(2L))),
    IDENT(CelMutableExpr.ofIdent("test")),
    SELECT(CelMutableExpr.ofSelect(CelMutableSelect.create(CelMutableExpr.ofNotSet(), "field"))),
    ;

    private final CelMutableExpr mutableExpr;

    MutableExprKindTestCase(CelMutableExpr mutableExpr) {
      this.mutableExpr = mutableExpr;
    }
  }

  @Test
  public void getExprValue_invalidKind_throws(@TestParameter MutableExprKindTestCase testCase) {
    Kind testCaseKind = testCase.mutableExpr.getKind();
    if (!testCaseKind.equals(Kind.NOT_SET)) {
      assertThrows(IllegalArgumentException.class, testCase.mutableExpr::notSet);
    }
    if (!testCaseKind.equals(Kind.CONSTANT)) {
      assertThrows(IllegalArgumentException.class, testCase.mutableExpr::constant);
    }
    if (!testCaseKind.equals(Kind.IDENT)) {
      assertThrows(IllegalArgumentException.class, testCase.mutableExpr::ident);
    }
    if (!testCaseKind.equals(Kind.SELECT)) {
      assertThrows(IllegalArgumentException.class, testCase.mutableExpr::select);
    }
  }

  @SuppressWarnings("Immutable") // Mutable by design
  private enum HashCodeTestCase {
    NOT_SET(CelMutableExpr.ofNotSet(1L), -722379961),
    CONSTANT(CelMutableExpr.ofConstant(2L, CelConstant.ofValue("test")), -724279919),
    IDENT(CelMutableExpr.ofIdent("x"), -721379855),
    SELECT(
        CelMutableExpr.ofSelect(
            4L,
            CelMutableSelect.create(CelMutableExpr.ofIdent("y"), "field", /* testOnly= */ true)),
        1458249843),
    ;

    private final CelMutableExpr mutableExpr;
    private final int expectedHashCode;

    HashCodeTestCase(CelMutableExpr mutableExpr, int expectedHashCode) {
      this.mutableExpr = mutableExpr;
      this.expectedHashCode = expectedHashCode;
    }
  }

  @Test
  public void hashCodeTest(@TestParameter HashCodeTestCase testCase) {
    assertThat(testCase.mutableExpr.hashCode()).isEqualTo(testCase.expectedHashCode);
    // Run it twice to ensure cached value is stable
    assertThat(testCase.mutableExpr.hashCode()).isEqualTo(testCase.expectedHashCode);
  }
}
