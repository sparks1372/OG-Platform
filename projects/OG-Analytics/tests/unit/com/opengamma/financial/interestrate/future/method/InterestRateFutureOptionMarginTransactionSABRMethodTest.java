/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the method for interest rate future option with SABR volatility parameter surfaces.
 */
public class InterestRateFutureOptionMarginTransactionSABRMethodTest {
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, CALENDAR, -SETTLEMENT_DAYS);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "EDU2";
  private static final InterestRateFutureSecurityDefinition EDU2_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAMES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final InterestRateFutureSecurity EDU2 = EDU2_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAMES);
  // Option 
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final double EXPIRATION_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRATION_DATE);
  private static final double STRIKE = 0.9850;
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurityDefinition OPTION_EDU2_DEFINITION = new InterestRateFutureOptionMarginSecurityDefinition(EDU2_DEFINITION, EXPIRATION_DATE, STRIKE, IS_CALL);
  private static final InterestRateFutureOptionMarginSecurity OPTION_EDU2 = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME, STRIKE, IS_CALL);
  // Transaction
  private static final int QUANTITY = -123;
  private static final double TRADE_PRICE = 0.0050;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransactionDefinition TRANSACTION_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_EDU2_DEFINITION, QUANTITY,
      TRADE_DATE, TRADE_PRICE);

  private static final InterestRateFutureOptionMarginTransactionSABRMethod METHOD = InterestRateFutureOptionMarginTransactionSABRMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecuritySABRMethod METHOD_SECURITY = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();

  private static final YieldCurveBundle CURVES_BUNDLE = TestsDataSets.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSets.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES_BUNDLE);
  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();

  @Test
  /**
   * Test the present value from the quoted option price.
   */
  public void presentValueFromOptionPrice() {
    final double priceQuoted = 0.01;
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
    final double pv = METHOD.presentValueFromPrice(transactionNoPremium, priceQuoted).getAmount();
    final double pvExpected = (priceQuoted - TRADE_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    assertEquals("Future option: present value from quoted price", pvExpected, pv);
  }

  @Test
  /**
   * Test the present value from the future price.
   */
  public void presentValueFromFuturePrice() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameters sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final double priceFuture = 0.9905;
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
    final double pv = METHOD.presentValueFromFuturePrice(transactionNoPremium, sabrBundle, priceFuture).getAmount();
    final double priceSecurity = METHOD_SECURITY.optionPriceFromFuturePrice(OPTION_EDU2, sabrBundle, priceFuture);
    final double pvExpected = (priceSecurity - TRADE_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    assertEquals("Future option: present value from future price", pvExpected, pv, 1.0E-2);
  }

  @Test
  /**
   * Test the present value from the future price.
   */
  public void presentValue() {
    final InterestRateFutureSecurityDiscountingMethod methodFuture = InterestRateFutureSecurityDiscountingMethod.getInstance();
    final double priceFuture = methodFuture.priceFromCurves(EDU2, CURVES_BUNDLE);
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
    final double pvNoPremium = METHOD.presentValue(transactionNoPremium, SABR_BUNDLE).getAmount();
    final double pvNoPremiumExpected = METHOD.presentValueFromFuturePrice(transactionNoPremium, SABR_BUNDLE, priceFuture).getAmount();
    assertEquals("Future option: present value", pvNoPremiumExpected, pvNoPremium);
  }

  @Test
  /**
   * Test the present value from the method and from the calculator.
   */
  public void presentValueMethodVsCalculator() {
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, 0.0);
    final double pvNoPremiumMethod = METHOD.presentValue(transactionNoPremium, SABR_BUNDLE).getAmount();
    final double pvNoPremiumCalculator = PVC.visit(transactionNoPremium, SABR_BUNDLE);
    assertEquals("Future option: present value: Method vs Calculator", pvNoPremiumMethod, pvNoPremiumCalculator);
  }

  @Test
  /**
   * Test the present value curves sensitivity computed from the curves
   */
  public void presentValueCurveSensitivity() {
    final PresentValueSensitivity pvsFuture = METHOD.presentValueCurveSensitivity(TRANSACTION, SABR_BUNDLE);
    pvsFuture.clean();
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    String[] curvesBumpedForward = new String[] {DISCOUNTING_CURVE_NAME, bumpedCurveName};
    InterestRateFutureOptionMarginTransaction transactionBumped = TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE, TRADE_PRICE, curvesBumpedForward);
    final double[] nodeTimesForward = new double[] {EDU2.getFixingPeriodStartTime(), EDU2.getFixingPeriodEndTime()};
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(transactionBumped, SABR_BUNDLE, FORWARD_CURVE_NAME, bumpedCurveName, nodeTimesForward, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvsFuture.getSensitivities().get(FORWARD_CURVE_NAME);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity future pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Tests that the method return the same result as the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final PresentValueCurveSensitivitySABRCalculator calculator = PresentValueCurveSensitivitySABRCalculator.getInstance();
    final Map<String, List<DoublesPair>> sensiCalculator = calculator.visit(TRANSACTION, SABR_BUNDLE);
    final PresentValueSensitivity sensiMethod = METHOD.presentValueCurveSensitivity(TRANSACTION, SABR_BUNDLE);
    assertEquals("Future option curve sensitivity: method comparison with present value calculator", sensiCalculator, sensiMethod.getSensitivities());
    InterestRateFutureOptionMarginSecuritySABRMethod methodSecurity = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();
    PresentValueSensitivity sensiSecurity = methodSecurity.priceCurveSensitivity(OPTION_EDU2, SABR_BUNDLE);
    PresentValueSensitivity sensiFromSecurity = sensiSecurity.multiply(QUANTITY * NOTIONAL * FUTURE_FACTOR);
    for (int looppt = 0; looppt < sensiMethod.getSensitivities().get(FORWARD_CURVE_NAME).size(); looppt++) {
      assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getSensitivities().get(FORWARD_CURVE_NAME).get(looppt).first, sensiFromSecurity
          .getSensitivities().get(FORWARD_CURVE_NAME).get(looppt).first, 1.0E-10);
      assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getSensitivities().get(FORWARD_CURVE_NAME).get(looppt).second, sensiFromSecurity
          .getSensitivities().get(FORWARD_CURVE_NAME).get(looppt).second, 1.0E-2);
    }
  }

  @Test
  public void presentValueSABRSensitivity() {
    final PresentValueSABRSensitivityDataBundle pvcs = METHOD.presentValueSABRSensitivity(TRANSACTION, SABR_BUNDLE);
    // SABR sensitivity vs finite difference
    final double pv = METHOD.presentValue(TRANSACTION, SABR_BUNDLE).getAmount();
    final double shift = 0.000001;
    double delay = EDU2.getLastTradingTime() - OPTION_EDU2.getExpirationTime();
    final DoublesPair expectedExpiryDelay = new DoublesPair(OPTION_EDU2.getExpirationTime(), delay);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = TestsDataSets.createSABR1AlphaBumped(shift);
    final SABRInterestRateDataBundle sabrBundleAlphaBumped = new SABRInterestRateDataBundle(sabrParameterAlphaBumped, CURVES_BUNDLE);
    final double pvAlphaBumped = METHOD.presentValue(TRANSACTION, sabrBundleAlphaBumped).getAmount();
    final double expectedAlphaSensi = (pvAlphaBumped - pv) / shift;
    assertEquals("Number of alpha sensitivity", pvcs.getAlpha().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvcs.getAlpha().keySet().contains(expectedExpiryDelay), true);
    assertEquals("Alpha sensitivity value", pvcs.getAlpha().get(expectedExpiryDelay), expectedAlphaSensi, 1.0E+1);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = TestsDataSets.createSABR1RhoBumped(shift);
    final SABRInterestRateDataBundle sabrBundleRhoBumped = new SABRInterestRateDataBundle(sabrParameterRhoBumped, CURVES_BUNDLE);
    final double pvRhoBumped = METHOD.presentValue(TRANSACTION, sabrBundleRhoBumped).getAmount();
    final double expectedRhoSensi = (pvRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvcs.getRho().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvcs.getRho().keySet().contains(expectedExpiryDelay), true);
    assertEquals("Rho sensitivity value", pvcs.getRho().get(expectedExpiryDelay), expectedRhoSensi, 1.0E+0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = TestsDataSets.createSABR1NuBumped(shift);
    final SABRInterestRateDataBundle sabrBundleNuBumped = new SABRInterestRateDataBundle(sabrParameterNuBumped, CURVES_BUNDLE);
    final double pvNuBumped = METHOD.presentValue(TRANSACTION, sabrBundleNuBumped).getAmount();
    final double expectedNuSensi = (pvNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvcs.getNu().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvcs.getNu().keySet().contains(expectedExpiryDelay), true);
    assertEquals("Nu sensitivity value", pvcs.getNu().get(expectedExpiryDelay), expectedNuSensi, 1.0E+0);
  }

  @Test
  /**
   * Tests that the method return the same result as the calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivitySABRCalculator calculator = PresentValueSABRSensitivitySABRCalculator.getInstance();
    final PresentValueSABRSensitivityDataBundle sensiCalculator = calculator.visit(TRANSACTION, SABR_BUNDLE);
    final PresentValueSABRSensitivityDataBundle sensiMethod = METHOD.presentValueSABRSensitivity(TRANSACTION, SABR_BUNDLE);
    assertEquals("Future option curve sensitivity: method comparison with present value calculator", sensiCalculator, sensiMethod);
    InterestRateFutureOptionMarginSecuritySABRMethod methodSecurity = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();
    PresentValueSABRSensitivityDataBundle sensiSecurity = methodSecurity.priceSABRSensitivity(OPTION_EDU2, SABR_BUNDLE);
    sensiSecurity.multiply(QUANTITY * NOTIONAL * FUTURE_FACTOR);
    assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getAlpha(), sensiSecurity.getAlpha());
    assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getRho(), sensiSecurity.getRho());
    assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getNu(), sensiSecurity.getNu());
  }

}
