/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;

/**
 * Test CurrencyUnit Fudge support.
 */
public class CurrencyTest extends FinancialTestBase {

  private static final Currency s_ref = Currency.USD;

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(Currency.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, s_ref.getCode())));
  }

  @Test
  public void testFromUniqueId() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, s_ref.getUniqueId().toString())));
  }

}
