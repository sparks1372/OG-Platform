/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;

/**
* A generalisation of a vanilla fixed for floating interest rate swap - here you must have a leg of FixedCouponPayment, but the other leg can be any payment 
* @param <R> The type of the payments on the receive leg 
*/
public class FixedCouponSwap<R extends Payment> extends Swap<CouponFixed, R> {

  /**
   * This sets up a generalised payer swap (i.e. pay the fixed leg and receive the other leg)
   * @param fixedLeg a fixed annuity for the receive leg
   * @param receiveLeg a variable (floating) annuity for the pay leg
   */
  public FixedCouponSwap(final GenericAnnuity<CouponFixed> fixedLeg, final GenericAnnuity<R> receiveLeg) {
    super(fixedLeg, receiveLeg);
  }

  public AnnuityCouponFixed getFixedLeg() {
    return (AnnuityCouponFixed) getFirstLeg();
  }

  /**
   * Check if the payments of of the other leg is of the type CouponFixed or CouponIbor. Used to check that payment are of vanilla type.
   * @return True if IborCoupon or FixedCoupon 
   */
  public boolean isIborOrFixed() {
    return getSecondLeg().isIborOrFixed();
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitFixedCouponSwap(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedCouponSwap(this);
  }

}
