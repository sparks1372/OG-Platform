/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.interestrate.PresentValueSABRExtrapolationCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.financial.interestrate.swaption.method.SwaptionCashFixedIborSABRExtrapolationRightMethod;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class to test the present value and present value rate sensitivity of the cash-settled European swaption in the SABR with extrapolation method. 
 * The SABR smile is extrapolated above a certain cut-off strike.
 */
public class SwaptionCashFixedIborSABRExtrapolationRightMethodTest {
  // Swaption description
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2014, 3, 18);
  private static final boolean IS_LONG = true;
  private static final int SETTLEMENT_DAYS = 2;
  // Swap 5Y description
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, BUSINESS_DAY, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.04;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  // Swaption construction
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_RECEIVER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_SHORT_PAYER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, !IS_LONG);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final SwaptionCashFixedIbor SWAPTION_LONG_PAYER = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_DEFINITION_LONG_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_DEFINITION_SHORT_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Extrapolation
  private static final double CUT_OFF_STRIKE = 0.08;
  private static final double MU = 10.0;
  private static final SwaptionCashFixedIborSABRExtrapolationRightMethod METHOD_EXTRAPOLATION = new SwaptionCashFixedIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
  // Calculators
  private static final PresentValueSABRExtrapolationCalculator PVC = PresentValueSABRExtrapolationCalculator.getInstance();

  /**
   * Tests present value in the region where there is no extrapolation. Tests long/short parity.
   */
  @Test
  public void testPresentValueNoExtra() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABRExtrapolation1(CUT_OFF_STRIKE, MU);
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final SwaptionCashFixedIborSABRExtrapolationRightMethod method = new SwaptionCashFixedIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
    final double priceLongPayer = method.presentValue(SWAPTION_LONG_PAYER, sabrBundle);
    final double priceShortPayer = method.presentValue(SWAPTION_SHORT_PAYER, sabrBundle);
    final double priceLongReceiver = method.presentValue(SWAPTION_LONG_RECEIVER, sabrBundle);
    final double priceShortReceiver = method.presentValue(SWAPTION_SHORT_RECEIVER, sabrBundle);
    final double priceLongPayerNoExtra = PVC.visit(SWAPTION_LONG_PAYER, sabrBundle);
    final double priceShortPayerNoExtra = PVC.visit(SWAPTION_SHORT_PAYER, sabrBundle);
    final double priceLongReceiverNoExtra = PVC.visit(SWAPTION_LONG_RECEIVER, sabrBundle);
    final double priceShortReceiverNoExtra = PVC.visit(SWAPTION_SHORT_RECEIVER, sabrBundle);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike", priceLongPayerNoExtra, priceLongPayer, 1E-2);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike", priceShortPayerNoExtra, priceShortPayer, 1E-2);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike", priceLongReceiverNoExtra, priceLongReceiver, 1E-2);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike", priceShortReceiverNoExtra, priceShortReceiver, 1E-2);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike long/short parity", priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals("Swaption cash SABR extrapolation: below cut-off strike long/short parity", priceLongReceiver, -priceShortReceiver, 1E-2);
  }

  /**
   * Tests present value in the region where there is extrapolation. Test a hard-coded value. Tests long/short parity. Test payer/receiver/swap parity.
   */
  @Test
  public void testPresentValueExtra() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double highStrike = 0.10;
    final SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER);
    final SwapFixedIborDefinition swapDefinitionReceiverHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, !FIXED_IS_PAYER);
    final SwaptionCashFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, IS_LONG);
    final SwaptionCashFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, !IS_LONG);
    final SwaptionCashFixedIborDefinition swaptionDefinitionLongReceiverHighStrike = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionReceiverHighStrike, IS_LONG);
    final SwaptionCashFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionCashFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionCashFixedIbor swaptionLongReceiverHighStrike = swaptionDefinitionLongReceiverHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionCashFixedIborSABRExtrapolationRightMethod method = new SwaptionCashFixedIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
    final double priceLongPayer = method.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    final double priceShortPayer = method.presentValue(swaptionShortPayerHighStrike, sabrBundle);
    final double priceLongReceiver = method.presentValue(swaptionLongReceiverHighStrike, sabrBundle);
    final double priceLongPayerExpected = 557829.033; // Value from previous run
    final double priceLongReceiverExpected = 20759354.082; // Value from previous run
    assertEquals("Swaption cash SABR extrapolation: fixed value", priceLongPayerExpected, priceLongPayer, 1E-2);
    assertEquals("Swaption cash SABR extrapolation: fixed value", priceLongReceiverExpected, priceLongReceiver, 1E-2);
    assertEquals("Swaption cash SABR extrapolation: long/short parity", priceLongPayer, -priceShortPayer, 1E-2);
  }

  @Test
  /**
   * Test the present value sensitivity to rate for a swaption with strike above the cut-off strike.
   */
  public void testPresentValueSensitivityExtra() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double highStrike = 0.10;
    final SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER);
    final SwaptionCashFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, IS_LONG);
    final SwaptionCashFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, !IS_LONG);
    final SwaptionCashFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionCashFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionCashFixedIborSABRExtrapolationRightMethod methodExtra = new SwaptionCashFixedIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
    // Swaption sensitivity
    PresentValueSensitivity pvsLongPayerExtra = methodExtra.presentValueSensitivity(swaptionLongPayerHighStrike, sabrBundle);
    final PresentValueSensitivity pvsShortPayerExtra = methodExtra.presentValueSensitivity(swaptionShortPayerHighStrike, sabrBundle);
    // Long/short parity
    final PresentValueSensitivity pvsShortPayer_1 = pvsShortPayerExtra.multiply(-1);
    assertEquals(pvsLongPayerExtra.getSensitivities(), pvsShortPayer_1.getSensitivities());
    // Present value sensitivity comparison with finite difference.
    final double deltaTolerance = 5.0E+4;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-5;
    pvsLongPayerExtra = pvsLongPayerExtra.clean();
    final double pv = methodExtra.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesForwardName = {FUNDING_CURVE_NAME, bumpedCurveName};
    final SwaptionCashFixedIbor swaptionBumpedForward = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, bumpedCurvesForwardName);
    final YieldAndDiscountCurve curveForward = curves.getCurve(FORWARD_CURVE_NAME);
    final Set<Double> timeForwardSet = new TreeSet<Double>();
    for (final Payment pay : swaptionLongPayerHighStrike.getUnderlyingSwap().getSecondLeg().getPayments()) {
      final CouponIbor coupon = (CouponIbor) pay;
      timeForwardSet.add(coupon.getFixingPeriodStartTime());
      timeForwardSet.add(coupon.getFixingPeriodEndTime());
    }
    final int nbForwardDate = timeForwardSet.size();
    final List<Double> timeForwardList = new ArrayList<Double>(timeForwardSet);
    Double[] timeForwardArray = new Double[nbForwardDate];
    timeForwardArray = timeForwardList.toArray(timeForwardArray);
    final double[] yieldsForward = new double[nbForwardDate + 1];
    final double[] nodeTimesForward = new double[nbForwardDate + 1];
    yieldsForward[0] = curveForward.getInterestRate(0.0);
    for (int i = 0; i < nbForwardDate; i++) {
      nodeTimesForward[i + 1] = timeForwardArray[i];
      yieldsForward[i + 1] = curveForward.getInterestRate(nodeTimesForward[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveForward = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesForward, yieldsForward, new LinearInterpolator1D()));
    final List<DoublesPair> tempForward = pvsLongPayerExtra.getSensitivities().get(FORWARD_CURVE_NAME);
    final double[] resFwd = new double[nbForwardDate];
    for (int i = 0; i < nbForwardDate; i++) {
      final YieldAndDiscountCurve bumpedCurveForward = tempCurveForward.withSingleShift(nodeTimesForward[i + 1], deltaShift);
      final YieldCurveBundle curvesBumpedForward = new YieldCurveBundle();
      curvesBumpedForward.addAll(curves);
      curvesBumpedForward.setCurve("Bumped Curve", bumpedCurveForward);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumpedForward);
      final double bumpedpv = methodExtra.presentValue(swaptionBumpedForward, sabrBundleBumped);
      resFwd[i] = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempForward.get(i);
      assertEquals("Sensitivity to forward curve: Node " + i, nodeTimesForward[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Sensitivity to forward curve: Node " + i, resFwd[i], pair.getSecond(), deltaTolerance);
    }
    // 2. Funding curve sensitivity
    final String[] bumpedCurvesFundingName = {bumpedCurveName, FORWARD_CURVE_NAME};
    final SwaptionCashFixedIbor swaptionBumpedFunding = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, bumpedCurvesFundingName);
    final int nbPayDate = swaptionDefinitionLongPayerHighStrike.getUnderlyingSwap().getIborLeg().getPayments().length;
    final YieldAndDiscountCurve curveFunding = curves.getCurve(FUNDING_CURVE_NAME);
    final double[] yieldsFunding = new double[nbPayDate + 2];
    final double[] nodeTimesFunding = new double[nbPayDate + 2];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    nodeTimesFunding[1] = swaptionLongPayerHighStrike.getSettlementTime();
    yieldsFunding[1] = curveFunding.getInterestRate(nodeTimesFunding[1]);
    for (int i = 0; i < nbPayDate; i++) {
      nodeTimesFunding[i + 2] = swaptionLongPayerHighStrike.getUnderlyingSwap().getSecondLeg().getNthPayment(i).getPaymentTime();
      yieldsFunding[i + 2] = curveFunding.getInterestRate(nodeTimesFunding[i + 2]);
    }
    final YieldAndDiscountCurve tempCurveFunding = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesFunding, yieldsFunding, new LinearInterpolator1D()));
    final List<DoublesPair> tempFunding = pvsLongPayerExtra.getSensitivities().get(FUNDING_CURVE_NAME);
    final double[] resDsc = new double[nbPayDate];
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimesFunding[i + 1], deltaShift);
      final YieldCurveBundle curvesBumped = new YieldCurveBundle();
      curvesBumped.addAll(curves);
      curvesBumped.setCurve("Bumped Curve", bumpedCurve);
      final SABRInterestRateDataBundle sabrBundleBumped = new SABRInterestRateDataBundle(sabrParameter, curvesBumped);
      final double bumpedpv = methodExtra.presentValue(swaptionBumpedFunding, sabrBundleBumped);
      resDsc[i] = (bumpedpv - pv) / deltaShift;
      final DoublesPair pair = tempFunding.get(i);
      assertEquals("Sensitivity to discounting curve: Node " + i, nodeTimesFunding[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Sensitivity to discounting curve: Node " + i, resDsc[i], pair.getSecond(), deltaTolerance);
    }
  }

  @Test
  /**
   * Test the present value sensitivity to SABR parameters for a swaption with strike above the cut-off strike.
   */
  public void testPresentValueSABRSensitivity() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double highStrike = 0.10;
    final SwapFixedIborDefinition swapDefinitionPayerHighStrike = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, highStrike, FIXED_IS_PAYER);
    final SwaptionCashFixedIborDefinition swaptionDefinitionLongPayerHighStrike = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, IS_LONG);
    final SwaptionCashFixedIborDefinition swaptionDefinitionShortPayerHighStrike = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapDefinitionPayerHighStrike, !IS_LONG);
    final SwaptionCashFixedIbor swaptionLongPayerHighStrike = swaptionDefinitionLongPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final SwaptionCashFixedIbor swaptionShortPayerHighStrike = swaptionDefinitionShortPayerHighStrike.toDerivative(REFERENCE_DATE, CURVES_NAME);
    //    SwaptionCashFixedIborSABRExtrapolationRightMethod methodExtra = new SwaptionCashFixedIborSABRExtrapolationRightMethod(CUT_OFF_STRIKE, MU);
    // Swaption sensitivity
    final PresentValueSABRSensitivityDataBundle pvsLongPayer = METHOD_EXTRAPOLATION.presentValueSABRSensitivity(swaptionLongPayerHighStrike, sabrBundle);
    final PresentValueSABRSensitivityDataBundle pvsShortPayer = METHOD_EXTRAPOLATION.presentValueSABRSensitivity(swaptionShortPayerHighStrike, sabrBundle);
    // Long/short parity
    pvsShortPayer.multiply(-1.0);
    assertEquals(pvsLongPayer.getAlpha(), pvsShortPayer.getAlpha());
    // SABR sensitivity vs finite difference
    final double pvLongPayer = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundle);
    final DoublesPair expectedExpiryTenor = new DoublesPair(swaptionLongPayerHighStrike.getTimeToExpiry(), ANNUITY_TENOR_YEAR);
    final double shift = 0.000005;
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSets.createSABR1AlphaBumped(shift);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, curves);
    final double pvLongPayerAlphaBumped = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundleAlphaBumped);
    final double expectedAlphaSensi = (pvLongPayerAlphaBumped - pvLongPayer) / shift;
    assertEquals("Number of alpha sensitivity", pvsLongPayer.getAlpha().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvsLongPayer.getAlpha().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Alpha sensitivity value", expectedAlphaSensi, pvsLongPayer.getAlpha().get(expectedExpiryTenor), 2.0E+3);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSets.createSABR1RhoBumped(shift);
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, curves);
    final double pvLongPayerRhoBumped = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundleRhoBumped);
    final double expectedRhoSensi = (pvLongPayerRhoBumped - pvLongPayer) / shift;
    assertEquals("Number of rho sensitivity", pvsLongPayer.getRho().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvsLongPayer.getRho().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Rho sensitivity value", expectedRhoSensi, pvsLongPayer.getRho().get(expectedExpiryTenor), 1.0E+0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSets.createSABR1NuBumped(shift);
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, curves);
    final double pvLongPayerNuBumped = METHOD_EXTRAPOLATION.presentValue(swaptionLongPayerHighStrike, sabrBundleNuBumped);
    final double expectedNuSensi = (pvLongPayerNuBumped - pvLongPayer) / shift;
    assertEquals("Number of nu sensitivity", pvsLongPayer.getNu().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvsLongPayer.getNu().keySet().contains(expectedExpiryTenor), true);
    assertEquals("Nu sensitivity value", expectedNuSensi, pvsLongPayer.getNu().get(expectedExpiryTenor), 5.0E+1);
  }

}
