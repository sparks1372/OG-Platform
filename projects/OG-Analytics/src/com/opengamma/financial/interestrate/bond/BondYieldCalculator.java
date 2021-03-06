/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.YieldSensitivityCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * Continuously compounded 
 */
public final class BondYieldCalculator extends BondCalculator {
  private static final YieldSensitivityCalculator YIELD_SENSITIVITY_CALCULATOR = YieldSensitivityCalculator.getInstance();
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final BondYieldCalculator INSTANCE = new BondYieldCalculator();

  private BondYieldCalculator() {
  }

  public static BondYieldCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double calculate(final Bond bond, final YieldCurveBundle curves) {
    return calculate(bond, DIRTY_PRICE_CALCULATOR.calculate(bond, curves));
  }

  @Override
  public Double calculate(final Bond bond, final double dirtyPrice) {
    Validate.notNull(bond, "bond");
    Validate.isTrue(dirtyPrice > 0.0, "need dirtyPrice greater than zero");
    return YIELD_SENSITIVITY_CALCULATOR.calculateYield(bond.getAnnuity(), dirtyPrice);
  }
}
