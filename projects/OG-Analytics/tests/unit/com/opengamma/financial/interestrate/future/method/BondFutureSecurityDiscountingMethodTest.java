/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.math.stat.descriptive.rank.Min;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.bond.method.BondSecurityDiscountingMethod;
import com.opengamma.financial.interestrate.future.calculator.PriceCurveSensitivityDiscountingCalculator;
import com.opengamma.financial.interestrate.future.calculator.PriceFromCurvesDiscountingCalculator;
import com.opengamma.financial.interestrate.future.definition.BondFutureSecurity;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the bond future figures computed by discounting.
 */
public class BondFutureSecurityDiscountingMethodTest {
  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final int NB_BOND = 7;
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5)};
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
      DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31)};
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175};
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292};
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, RATE[loopbasket], SETTLEMENT_DAYS, CALENDAR,
          DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
    }
  }
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 30);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 10, 4);
  private static final ZonedDateTime FIRST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(FIRST_NOTICE_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final ZonedDateTime LAST_DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(LAST_NOTICE_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
  private static final double FIRST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_NOTICE_DATE);
  private static final double LAST_NOTICE_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_NOTICE_DATE);
  private static final double FIRST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIRST_DELIVERY_DATE);
  private static final double LAST_DELIVERY_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_DELIVERY_DATE);
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurvesBond1();
  private static final BondFixedSecurity[] BASKET = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] STANDARD = new BondFixedSecurity[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      BASKET[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, LAST_DELIVERY_DATE, CURVES_NAME);
      STANDARD[loopbasket] = BASKET_DEFINITION[loopbasket].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
  }
  private static final BondFutureSecurity BOND_FUTURE_SECURITY = new BondFutureSecurity(LAST_TRADING_TIME, FIRST_NOTICE_TIME, LAST_NOTICE_TIME, FIRST_DELIVERY_TIME, LAST_DELIVERY_TIME, NOTIONAL,
      BASKET, CONVERSION_FACTOR);
  private static final BondFutureSecurityDiscountingMethod METHOD = BondFutureSecurityDiscountingMethod.getInstance();
  private static final BondSecurityDiscountingMethod METHOD_BOND = BondSecurityDiscountingMethod.getInstance();
  private static final PriceFromCurvesDiscountingCalculator PRICE_CALCULATOR = PriceFromCurvesDiscountingCalculator.getInstance();
  private static final PriceCurveSensitivityDiscountingCalculator PRICE_SENSI_CALCULATOR = PriceCurveSensitivityDiscountingCalculator.getInstance();

  @Test
  public void price() {
    final double priceComputed = METHOD.priceFromCurves(BOND_FUTURE_SECURITY, CURVES);
    final double[] bondForwardPrice = new double[NB_BOND];
    final double[] bondForwardPriceAdjusted = new double[NB_BOND];
    double priceExpected = 2.0;
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      bondForwardPrice[loopbasket] = METHOD_BOND.cleanPriceFromCurves(BASKET[loopbasket], CURVES);
      bondForwardPriceAdjusted[loopbasket] = bondForwardPrice[loopbasket] / CONVERSION_FACTOR[loopbasket];
      priceExpected = Math.min(priceExpected, bondForwardPriceAdjusted[loopbasket]);
    }
    assertEquals("Bond future security Discounting Method: price from curves", priceExpected, priceComputed, 1.0E-10);
  }

  @Test
  /**
   * Tests the computation of the price curve sensitivity.
   */
  public void priceCurveSensitivity() {
    PresentValueSensitivity sensiFuture = METHOD.priceCurveSensitivity(BOND_FUTURE_SECURITY, CURVES);
    final double[] bondForwardPrice = new double[NB_BOND];
    final double[] bondFuturePrice = new double[NB_BOND];
    double minPrice = 100.0;
    int minIndex = 0;
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      bondForwardPrice[loopbasket] = METHOD_BOND.dirtyPriceFromCurves(BASKET[loopbasket], CURVES);
      bondFuturePrice[loopbasket] = (bondForwardPrice[loopbasket] - BASKET[loopbasket].getAccruedInterest()) / CONVERSION_FACTOR[loopbasket];
      if (bondFuturePrice[loopbasket] < minPrice) {
        minPrice = (bondForwardPrice[loopbasket] - BASKET[loopbasket].getAccruedInterest()) / CONVERSION_FACTOR[loopbasket];
        minIndex = loopbasket;
      }
    }
    PresentValueSensitivity sensiBond = METHOD_BOND.dirtyPriceCurveSensitivity(BASKET[minIndex], CURVES);
    sensiBond = sensiBond.multiply(1.0 / CONVERSION_FACTOR[minIndex]);
    sensiFuture = sensiFuture.clean();
    sensiBond = sensiBond.clean();
    for (int loopsensi = 0; loopsensi < sensiFuture.getSensitivities().get(CREDIT_CURVE_NAME).size(); loopsensi++) {
      assertEquals("Bond future security Discounting Method: curve sensitivity " + loopsensi, sensiBond.getSensitivities().get(CREDIT_CURVE_NAME).get(loopsensi).first,
          sensiFuture.getSensitivities().get(CREDIT_CURVE_NAME).get(loopsensi).first, 1.0E-10);
      assertEquals("Bond future security Discounting Method: curve sensitivity " + loopsensi, sensiBond.getSensitivities().get(CREDIT_CURVE_NAME).get(loopsensi).second, sensiFuture.getSensitivities()
          .get(CREDIT_CURVE_NAME).get(loopsensi).second, 1.0E-10);
    }
  }

  @Test
  /**
   * Tests the computation of the price curve sensitivity.
   */
  public void priceCurveSensitivityMethodVsCalculator() {
    final PresentValueSensitivity sensiMethod = METHOD.priceCurveSensitivity(BOND_FUTURE_SECURITY, CURVES);
    final PresentValueSensitivity sensiCalculator = PRICE_SENSI_CALCULATOR.visit(BOND_FUTURE_SECURITY, CURVES);
    assertEquals("Bond future security Discounting Method: curve sensitivity Method versus Calculator", sensiMethod, sensiCalculator);
  }

  @Test
  /**
   * Tests the method versus the calculator for the price.
   */
  public void priceMethodVsCalculator() {
    final double priceMethod = METHOD.priceFromCurves(BOND_FUTURE_SECURITY, CURVES);
    final double priceCalculator = PRICE_CALCULATOR.visit(BOND_FUTURE_SECURITY, CURVES);
    assertEquals("Bond future security Discounting: Method vs calculator", priceMethod, priceCalculator, 1.0E-10);
  }

  @Test
  /**
   * Tests the net basis computed from the curves.
   */
  public void netBasis() {
    //final double priceFuture = 1.0320;
    final double priceFuture = METHOD.priceFromCurves(BOND_FUTURE_SECURITY, CURVES);
    final double[] netBasisComputed = METHOD.netBasisFromCurves(BOND_FUTURE_SECURITY, CURVES, priceFuture);
    final double[] netBasisExpected = new double[NB_BOND];
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      final double bondPriceForward = METHOD_BOND.dirtyPriceFromCurves(BOND_FUTURE_SECURITY.getDeliveryBasket()[loopbasket], CURVES);
      netBasisExpected[loopbasket] = bondPriceForward - (priceFuture * CONVERSION_FACTOR[loopbasket] + BOND_FUTURE_SECURITY.getDeliveryBasket()[loopbasket].getAccruedInterest());
      assertEquals("Bond future security Discounting Method: netBasis", netBasisExpected[loopbasket], netBasisComputed[loopbasket], 1.0E-10);
    }
    final Min minFunction = new Min();
    final double netBasisMin = minFunction.evaluate(netBasisComputed);
    final double priceFutureFromNetBasis = METHOD.priceFromCurvesAndNetBasis(BOND_FUTURE_SECURITY, CURVES, netBasisMin);
    assertEquals("Bond future security Discounting Method: netBasis", priceFuture, priceFutureFromNetBasis, 1.0E-10);
  }

  @Test
  /**
   * Tests the cheapest to deliver figures: yield, modified duration and gross basis.
   */
  public void cheapestToDeliver() {
    final double yieldTest = 0.01345;
    final double priceTest = 1.03414063;
    final double mdTest = 4.271;
    final double grossBasisTest = 20.718; // Quoted in 32ds of %
    final double futurePriceTest = 1.19984375;
    final double[] priceCTD = new double[NB_BOND];
    final double[] mdCTD = new double[NB_BOND];
    final double[] yieldCTD = new double[NB_BOND];
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      priceCTD[loopbasket] = priceTest;
      mdCTD[loopbasket] = METHOD_BOND.modifiedDurationFromYield(STANDARD[loopbasket], yieldTest);
      yieldCTD[loopbasket] = METHOD_BOND.yieldFromCleanPrice(STANDARD[loopbasket], priceTest);
    }
    final double[] grossBasis = METHOD.grossBasisFromPrices(BOND_FUTURE_SECURITY, priceCTD, futurePriceTest);
    final int ctdIndex = 1;
    assertEquals("Bond future security: CTD - yield from price", yieldTest, yieldCTD[ctdIndex], 1.0E-4);
    assertEquals("Bond future security: CTD - modified duration from yield", mdTest, mdCTD[ctdIndex], 1.0E-3);
    assertEquals("Bond future security: CTD - gross basis from price", grossBasisTest / 100.0 / 32.0, grossBasis[ctdIndex], 1.0E-7);
  }

  @Test
  /**
   * Tests the gross basis computed from clean prices
   */
  public void grossBasis() {
    final double futurePriceTest = 1.19984375;
    final double[] pricesTest = new double[] {0.86, 0.885, 0.88, 0.8825, 0.885, 0.8725, 0.86};
    final double[] pricesCurves = new double[NB_BOND];
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      pricesCurves[loopbasket] = METHOD_BOND.cleanPriceFromCurves(BOND_FUTURE_SECURITY.getDeliveryBasket()[loopbasket], CURVES);
    }
    final double[] basisComputedTest = METHOD.grossBasisFromPrices(BOND_FUTURE_SECURITY, pricesTest, futurePriceTest);
    final double[] basisComputedCurves = METHOD.grossBasisFromPrices(BOND_FUTURE_SECURITY, pricesCurves, futurePriceTest);
    final double[] basisExpectedTest = new double[NB_BOND];
    final double[] basisExpectedCurves = new double[NB_BOND];
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      basisExpectedTest[loopbasket] = (pricesTest[loopbasket] - futurePriceTest * CONVERSION_FACTOR[loopbasket]);
      basisExpectedCurves[loopbasket] = (pricesCurves[loopbasket] - futurePriceTest * CONVERSION_FACTOR[loopbasket]);
      assertEquals("Gross basis from prices", basisExpectedTest[loopbasket], basisComputedTest[loopbasket], 1.0E-10);
      assertEquals("Gross basis from curves", basisExpectedCurves[loopbasket], basisComputedCurves[loopbasket], 1.0E-10);
    }
  }

}
