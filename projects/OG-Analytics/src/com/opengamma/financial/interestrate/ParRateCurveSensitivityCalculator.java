/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.financial.interestrate.payments.CouponOIS;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, this calculates the sensitivity of the par rate (the exact meaning of par rate depends on the instrument - for swaps it is the par swap rate) to points on the yield 
 * curve(s) (i.e. dPar/dR at every point the instrument has sensitivity). The return format is a map with curve names (String) as keys and List of DoublesPair as the values; each list holds 
 * set of time (corresponding to point of the yield curve) and sensitivity pairs (i.e. dPar/dR at that time). 
 * <b>Note:</b> The length of the list is instrument dependent and may have repeated times (with the understanding the sensitivities should be summed).
 */
public final class ParRateCurveSensitivityCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> {
  private static final PresentValueCalculator PV_CALCULATOR = PresentValueCalculator.getInstance();
  private static final PresentValueSensitivityCalculator SENSITIVITY_CALCULATOR = PresentValueSensitivityCalculator.getInstance();
  private static final RateReplacingInterestRateDerivativeVisitor REPLACE_RATE = RateReplacingInterestRateDerivativeVisitor.getInstance();
  private static final ParRateCurveSensitivityCalculator s_instance = new ParRateCurveSensitivityCalculator();

  public static ParRateCurveSensitivityCalculator getInstance() {
    return s_instance;
  }

  private ParRateCurveSensitivityCalculator() {
  }

  @Override
  public Map<String, List<DoublesPair>> visit(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    Validate.notNull(instrument);
    Validate.notNull(curves);
    return instrument.accept(this, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCash(final Cash cash, final YieldCurveBundle curves) {
    final String curveName = cash.getYieldCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = cash.getTradeTime();
    final double tb = cash.getMaturity();
    final double yearFrac = cash.getYearFraction();
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    if (yearFrac == 0.0) {
      if (!CompareUtils.closeEquals(ta, tb, 1e-16)) {
        throw new IllegalArgumentException("year fraction is zero, but payment time not equal the trade time");
      }
      temp.add(new DoublesPair(ta, 1.0));
    } else {
      final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / yearFrac;
      temp.add(new DoublesPair(ta, -ta * ratio));
      temp.add(new DoublesPair(tb, tb * ratio));
    }
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    final ForwardRateAgreementDiscountingMethod method = ForwardRateAgreementDiscountingMethod.getInstance();
    return method.parRateCurveSensitivity(fra, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitInterestRateFutureSecurity(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    final String curveName = future.getForwardCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = future.getFixingPeriodStartTime();
    final double tb = future.getFixingPeriodEndTime();
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / future.getFixingPeriodAccrualFactor();
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    return visitInterestRateFutureSecurity(future.getUnderlyingFuture(), curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    final AnnuityCouponFixed unitCouponAnnuity = REPLACE_RATE.visitFixedCouponAnnuity(swap.getFixedLeg(), 1.0);
    final GenericAnnuity<?> floatingAnnuity = swap.getSecondLeg();
    final double a = PV_CALCULATOR.visit(unitCouponAnnuity, curves);
    final double b = PV_CALCULATOR.visit(floatingAnnuity, curves);
    final double bOveraSq = b / a / a;
    final Map<String, List<DoublesPair>> senseA = SENSITIVITY_CALCULATOR.visit(unitCouponAnnuity, curves);
    final Map<String, List<DoublesPair>> senseB = SENSITIVITY_CALCULATOR.visit(floatingAnnuity, curves);

    //TODO: Refactor the code below to use PresentValueSensitivity add/multiply
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : curves.getAllNames()) {
      boolean flag = false;
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      if (senseA.containsKey(name)) {
        flag = true;
        for (final DoublesPair pair : senseA.get(name)) {
          final double t = pair.getFirst();
          final DoublesPair newPair = new DoublesPair(t, bOveraSq * pair.getSecond());
          temp.add(newPair);
        }
      }
      if (senseB.containsKey(name)) {
        flag = true;
        for (final DoublesPair pair : senseB.get(name)) {
          final double t = pair.getFirst();
          final DoublesPair newPair = new DoublesPair(t, -pair.getSecond() / a);
          temp.add(newPair);
        }
      }
      if (flag) {
        result.put(name, temp);
      }
    }
    return result;
  }

  /**
   * The assumption is that spread is received (i.e. the spread, if any, is on the received leg only)
   * If the spread is paid (i.e. on the pay leg), swap the legs around and take the negative of the returned value.
   * @param swap 
   * @param curves 
   * @return  The spread on the receive leg of a Tenor swap 
   */
  @Override
  public Map<String, List<DoublesPair>> visitTenorSwap(final TenorSwap<? extends Payment> swap, final YieldCurveBundle curves) {
    final AnnuityCouponIbor payLeg = ((AnnuityCouponIbor) swap.getFirstLeg()).withZeroSpread();
    final AnnuityCouponIbor receiveLeg = ((AnnuityCouponIbor) swap.getSecondLeg()).withZeroSpread();
    final AnnuityCouponFixed spreadLeg = receiveLeg.withUnitCoupons();

    final double a = PV_CALCULATOR.visit(receiveLeg, curves);
    final double b = PV_CALCULATOR.visit(payLeg, curves);
    final double c = PV_CALCULATOR.visit(spreadLeg, curves);

    final Map<String, List<DoublesPair>> senseA = SENSITIVITY_CALCULATOR.visit(receiveLeg, curves);
    final Map<String, List<DoublesPair>> senseB = SENSITIVITY_CALCULATOR.visit(payLeg, curves);
    final Map<String, List<DoublesPair>> senseC = SENSITIVITY_CALCULATOR.visit(spreadLeg, curves);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();

    final double factor = (b + a) / c / c;

    for (final String name : curves.getAllNames()) {
      boolean flag = false;
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      if (senseA.containsKey(name)) {
        flag = true;
        for (final DoublesPair pair : senseA.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), -pair.getSecond() / c));
        }
      }
      if (senseB.containsKey(name)) {
        flag = true;
        for (final DoublesPair pair : senseB.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), -pair.getSecond() / c));
        }
      }
      if (senseC.containsKey(name)) {
        flag = true;
        for (final DoublesPair pair : senseC.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), factor * pair.getSecond()));
        }
      }
      if (flag) {
        result.put(name, temp);
      }
    }
    return result;
  }

  //  @Override
  //  public Map<String, List<DoublesPair>> visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
  //    return visitSwap(frn, curves);
  //  }

  @Override
  public Map<String, List<DoublesPair>> visitBond(final Bond bond, final YieldCurveBundle curves) {
    final GenericAnnuity<CouponFixed> coupons = bond.getUnitCouponAnnuity();
    final double a = PV_CALCULATOR.visit(coupons, curves);
    final Map<String, List<DoublesPair>> senseA = SENSITIVITY_CALCULATOR.visit(coupons, curves);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();

    final PaymentFixed principlePaymemt = bond.getPrinciplePayment();
    final double df = PV_CALCULATOR.visit(principlePaymemt, curves);
    final double factor = -(1 - df) / a / a;

    for (final String name : curves.getAllNames()) {
      if (senseA.containsKey(name)) {
        final List<DoublesPair> temp = new ArrayList<DoublesPair>();
        final List<DoublesPair> list = senseA.get(name);
        final int n = list.size();
        for (int i = 0; i < (n - 1); i++) {
          final DoublesPair pair = list.get(i);
          temp.add(new DoublesPair(pair.getFirst(), factor * pair.getSecond()));
        }
        final DoublesPair pair = list.get(n - 1);
        temp.add(new DoublesPair(pair.getFirst(), principlePaymemt.getPaymentTime() * df / a + factor * pair.getSecond()));
        result.put(name, temp);
      }
    }
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponIbor(final CouponIbor payment, final YieldCurveBundle data) {
    final String curveName = payment.getForwardCurveName();
    final YieldAndDiscountCurve curve = data.getCurve(curveName);
    //    final double ta = payment.getFixingTime();
    final double ta = payment.getFixingPeriodStartTime();
    final double tb = payment.getFixingPeriodEndTime();
    final double delta = payment.getFixingYearFraction();
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / delta;
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitCapFloorIbor(final CapFloorIbor payment, final YieldCurveBundle data) {
    return visitCouponIbor(payment, data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponOIS(final CouponOIS payment, final YieldCurveBundle data) {
    final double ta = payment.getStartTime();
    final double tb = payment.getEndTime();

    final DoublesPair s1 = new DoublesPair(ta, -ta / payment.getRateYearFraction());
    final DoublesPair s2 = new DoublesPair(tb, tb / payment.getRateYearFraction());
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(payment.getFundingCurveName(), temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle data) {
    return visitFixedCouponSwap(swap, data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponIborFixed(CouponIborFixed payment, YieldCurveBundle data) {
    return visitCouponIbor(payment.toCouponIbor(), data);
  }

}
