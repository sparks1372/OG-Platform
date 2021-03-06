/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.AbstractInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.future.definition.BondFutureTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.future.method.BondFutureTransactionDiscountingMethod;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureTransactionDiscountingMethod;

/**
 * Calculate present value for futures from the quoted price.
 */
public final class PresentValueFromFuturePriceCalculator extends AbstractInterestRateDerivativeVisitor<Double, Double> {

  /**
   * The calculator instance.
   */
  private static final PresentValueFromFuturePriceCalculator s_instance = new PresentValueFromFuturePriceCalculator();
  /**
   * The method to compute bond future prices.
   */
  private static final BondFutureTransactionDiscountingMethod METHOD_BOND_FUTURE = BondFutureTransactionDiscountingMethod.getInstance();
  /**
   * The method to compute interest rate future prices.
   */
  private static final InterestRateFutureTransactionDiscountingMethod METHOD_RATE_FUTURE = InterestRateFutureTransactionDiscountingMethod.getInstance();

  /**
   * Return the calculator instance.
   * @return The instance.
   */
  public static PresentValueFromFuturePriceCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueFromFuturePriceCalculator() {
  }

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final Double futurePrice) {
    Validate.notNull(future);
    return METHOD_RATE_FUTURE.presentValueFromPrice(future, futurePrice);
  }

  @Override
  public Double visitBondFutureTransaction(final BondFutureTransaction future, final Double futurePrice) {
    Validate.notNull(future);
    return METHOD_BOND_FUTURE.presentValueFromPrice(future, futurePrice);
  }

}
