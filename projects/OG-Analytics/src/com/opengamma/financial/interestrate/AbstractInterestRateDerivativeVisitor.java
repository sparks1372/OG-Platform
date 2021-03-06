/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.bond.definition.BondIborSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.BondFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.BondFutureTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCoupon;
import com.opengamma.financial.interestrate.payments.CapFloorCMS;
import com.opengamma.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.CouponOIS;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public abstract class AbstractInterestRateDerivativeVisitor<S, T> implements InterestRateDerivativeVisitor<S, T> {

  @Override
  public T visit(final InterestRateDerivative derivative, final S data) {
    Validate.notNull(derivative, "derivative");
    Validate.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public T[] visit(final InterestRateDerivative[] derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[], data)");
  }

  @Override
  public T visit(final InterestRateDerivative derivative) {
    Validate.notNull(derivative, "derivative");
    return derivative.accept(this);
  }

  @Override
  public T[] visit(final InterestRateDerivative[] derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(derivative[])");
  }

  @Override
  public T visitBond(final Bond bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBond()");
  }

  @Override
  public T visitBondForward(final BondForward bondForward, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondForward()");
  }

  @Override
  public T visitBondFixedSecurity(final BondFixedSecurity bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedSecurity()");
  }

  @Override
  public T visitBondFixedTransaction(final BondFixedTransaction bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedTransaction()");
  }

  @Override
  public T visitBondIborSecurity(final BondIborSecurity bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborSecurity()");
  }

  @Override
  public T visitBondIborTransaction(final BondIborTransaction bond, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborTransaction()");
  }

  @Override
  public T visitBondFuture(final BondFuture bondFuture, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFuture()");
  }

  @Override
  public T visitBondFutureSecurity(final BondFutureSecurity bondFuture, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureSecurity()");
  }

  @Override
  public T visitBondFutureTransaction(final BondFutureTransaction bondFuture, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureSecurity()");
  }

  @Override
  public T visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponAnnuity()");
  }

  @Override
  public T visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardLiborAnnuity()");
  }

  @Override
  public T visitFixedFloatSwap(final FixedFloatSwap swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedFloatSwap()");
  }

  @Override
  public T visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitTenorSwap()");
  }

  @Override
  public T visitCash(final Cash cash, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCash()");
  }

  @Override
  public T visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureSecurity()");
  }

  @Override
  public T visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureTransaction()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginTransaction()");
  }

  @Override
  public T visitCouponOIS(final CouponOIS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitContinuouslyMonitoredAverageRatePayment()");
  }

  @Override
  public T visitFixedCouponPayment(final CouponFixed payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponPayment()");
  }

  @Override
  public T visitCouponIbor(final CouponIbor payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIbor()");
  }

  @Override
  public T visitCouponIborGearing(final CouponIborGearing payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborGearing()");
  }

  @Override
  public T visitCapFloorIbor(final CapFloorIbor payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorIbor()");
  }

  @Override
  public T visitCouponCMS(final CouponCMS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMS()");
  }

  @Override
  public T visitCapFloorCMS(final CapFloorCMS payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMS()");
  }

  @Override
  public T visitCapFloorCMSSpread(final CapFloorCMSSpread payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMSSpread()");
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitGenericAnnuity(final GenericAnnuity<? extends Payment> genericAnnuity, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitGenericAnnuity()");
  }

  @Override
  public T visitSwap(final Swap<?, ?> swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitFixedCouponSwap(final FixedCouponSwap<?> swap, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponSwap()");
  }

  @Override
  public T visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionCashFixedIbor()");
  }

  @Override
  public T visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionPhysicalFixedIbor()");
  }

  @Override
  public T visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionBermudaFixedIbor()");
  }

  @Override
  public T visitFixedPayment(final PaymentFixed payment, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedPayment()");
  }

  @Override
  public T visitBond(final Bond bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBond()");
  }

  @Override
  public T visitBondForward(final BondForward bondForward) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondForward()");
  }

  @Override
  public T visitBondFixedSecurity(final BondFixedSecurity bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedSecurity()");
  }

  @Override
  public T visitBondFixedTransaction(final BondFixedTransaction bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFixedTransaction()");
  }

  @Override
  public T visitBondIborSecurity(final BondIborSecurity bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborSecurity()");
  }

  @Override
  public T visitBondIborTransaction(final BondIborTransaction bond) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondIborTransaction()");
  }

  @Override
  public T visitBondFuture(final BondFuture bondFuture) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFuture()");
  }

  @Override
  public T visitBondFutureSecurity(final BondFutureSecurity bondFuture) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureSecurity()");
  }

  @Override
  public T visitBondFutureTransaction(final BondFutureTransaction bondFuture) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitBondFutureSecurity()");
  }

  @Override
  public T visitFixedCouponAnnuity(final AnnuityCouponFixed fixedCouponAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponAnnuity()");
  }

  @Override
  public T visitForwardLiborAnnuity(final AnnuityCouponIbor forwardLiborAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardLiborAnnuity()");
  }

  @Override
  public T visitFixedFloatSwap(final FixedFloatSwap swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedFloatSwap()");
  }

  @Override
  public T visitFloatingRateNote(final FloatingRateNote frn) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFloatingRateNote()");
  }

  @Override
  public T visitTenorSwap(final TenorSwap<? extends Payment> tenorSwap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitTenorSwap()");
  }

  @Override
  public T visitCash(final Cash cash) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCash()");
  }

  @Override
  public T visitInterestRateFutureSecurity(final InterestRateFutureSecurity future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureTransaction()");
  }

  @Override
  public T visitInterestRateFutureTransaction(final InterestRateFutureTransaction future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureTransaction()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionPremiumSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginSecurity()");
  }

  @Override
  public T visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitInterestRateFutureOptionMarginTransaction()");
  }

  @Override
  public T visitCouponOIS(final CouponOIS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitContinuouslyMonitoredAverageRatePayment()");
  }

  @Override
  public T visitFixedCouponPayment(final CouponFixed payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponPayment()");
  }

  @Override
  public T visitCouponIbor(final CouponIbor payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIbor()");
  }

  @Override
  public T visitCouponIborGearing(final CouponIborGearing payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborGearing()");
  }

  @Override
  public T visitCapFloorIbor(final CapFloorIbor payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorIbor()");
  }

  @Override
  public T visitCouponCMS(final CouponCMS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponCMS()");
  }

  @Override
  public T visitCapFloorCMS(final CapFloorCMS payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMS()");
  }

  @Override
  public T visitCapFloorCMSSpread(final CapFloorCMSSpread payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCapFloorCMSSpread()");
  }

  @Override
  public T visitForwardRateAgreement(final ForwardRateAgreement fra) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitForwardRateAgreement()");
  }

  @Override
  public T visitGenericAnnuity(final GenericAnnuity<? extends Payment> genericAnnuity) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitGenericAnnuity()");
  }

  @Override
  public T visitSwap(final Swap<?, ?> swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwap()");
  }

  @Override
  public T visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionCashFixedIbor()");
  }

  @Override
  public T visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionPhysicalFixedIbor()");
  }

  @Override
  public T visitSwaptionBermudaFixedIbor(final SwaptionBermudaFixedIbor swaption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitSwaptionBermudaFixedIbor()");
  }

  @Override
  public T visitFixedCouponSwap(final FixedCouponSwap<?> swap) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedCouponSwap()");
  }

  @Override
  public T visitFixedPayment(final PaymentFixed payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitFixedPayment()");
  }

  @Override
  public T visitCouponIborFixed(CouponIborFixed payment) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborFixed()");
  }

  @Override
  public T visitCouponIborFixed(CouponIborFixed payment, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborFixed()");
  }

  @Override
  public T visitCouponInflationZeroCoupon(CouponInflationZeroCoupon coupon) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborFixed()");
  }

  @Override
  public T visitCouponInflationZeroCoupon(CouponInflationZeroCoupon coupon, S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitCouponIborFixed()");
  }

}
