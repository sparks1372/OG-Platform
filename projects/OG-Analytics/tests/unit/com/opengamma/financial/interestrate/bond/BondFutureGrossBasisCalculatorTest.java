/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureDeliverableBasketDataBundle;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondFutureGrossBasisCalculatorTest {
  private static final BondFutureGrossBasisCalculator CALCULATOR = BondFutureGrossBasisCalculator.getInstance();
  private static final String NAME = "A";
  private static final Currency CUR = Currency.USD;
  private static final BondForward[] DELIVERABLES = new BondForward[] {new BondForward(new Bond(CUR, new double[] {1, 2, 3}, 0.05, NAME), 0.1, 0, 0),
      new BondForward(new Bond(CUR, new double[] {1, 2, 3, 4, 5, 6}, 0.06, NAME), 0.1, 0, 0.04), new BondForward(new Bond(CUR, new double[] {1, 2, 3, 4, 5}, 0.045, NAME), 0.2, 0, 1)};
  private static final double[] CONVERSION_FACTORS = new double[] {0.123, 0.456, 0.789};
  private static final double[] CLEAN_PRICES = new double[] {97., 98., 99.};
  private static final double[] REPO_RATES = new double[] {0.03, 0.02, 0.03};
  private static final BondFutureDeliverableBasketDataBundle BASKET_DATA = new BondFutureDeliverableBasketDataBundle(CLEAN_PRICES, REPO_RATES);
  private static final double FUTURE_PRICE = 105;
  private static final BondFuture FUTURE = new BondFuture(DELIVERABLES, CONVERSION_FACTORS, FUTURE_PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBondFuture() {
    CALCULATOR.calculate(null, BASKET_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBasketData() {
    CALCULATOR.calculate(FUTURE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongBasketSize() {
    CALCULATOR.calculate(new BondFuture(new BondForward[] {DELIVERABLES[0]}, new double[] {0.78}, FUTURE_PRICE), BASKET_DATA);
  }

  @Test
  public void test() {
    final double[] grossBases = CALCULATOR.calculate(FUTURE, BASKET_DATA);
    assertEquals(grossBases.length, 3);
    final double[] result = new double[] {97 - FUTURE_PRICE * 0.123, 98 - FUTURE_PRICE * 0.456, 99 - FUTURE_PRICE * 0.789};
    assertArrayEquals(grossBases, result, 1e-15);
  }
}
