/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class used to compute the price and sensitivity of a Ibor cap/floor with SABR model. 
 *  No convexity adjustment is done for payment at non-standard dates.
 */
public final class CapFloorIborSABRMethod implements PricingMethod {

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  /**
   * The Par Rate Calculator used in the pricing.
   */
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  /**
   * The par rate sensitivity calculator.
   */
  private static final ParRateCurveSensitivityCalculator PRSC = ParRateCurveSensitivityCalculator.getInstance();
  private static final CapFloorIborSABRMethod INSTANCE = new CapFloorIborSABRMethod();

  public static CapFloorIborSABRMethod getInstance() {
    return INSTANCE;
  }

  private CapFloorIborSABRMethod() {
  }

  /**
   * Computes the present value of a cap/floor in the SABR model.
   * @param cap The cap/floor.
   * @param sabrData The SABR data bundle. 
   * @return The present value.
   */
  public CurrencyAmount presentValue(final CapFloorIbor cap, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cap);
    Validate.notNull(sabrData);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = PRC.visit(cap, sabrData);
    final double df = sabrData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getPaymentTime());
    final double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    // TODO: Improve maturity, using periods?
    final double volatility = sabrData.getSABRParameter().getVolatility(cap.getFixingTime(), maturity, cap.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, df, volatility);
    final Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(option);
    final double price = func.evaluate(dataBlack) * cap.getNotional() * cap.getPaymentYearFraction();
    return CurrencyAmount.of(cap.getCurrency(), price);
  }

  @Override
  public CurrencyAmount presentValue(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CapFloorIbor, "Cap/Floor on Ibor");
    Validate.isTrue(curves instanceof SABRInterestRateDataBundle, "SABR interest rate data bundle required");
    return presentValue((CapFloorIbor) instrument, (SABRInterestRateDataBundle) curves);
  }

  /**
   * Computes the present value rate sensitivity to rates of a cap/floor in the SABR model.
   * @param cap The cap/floor.
   * @param sabrData The SABR data bundle. The SABR function need to be the Hagan function.
   * @return The present value curve sensitivity.
   */
  public PresentValueSensitivity presentValueSensitivity(final CapFloorIbor cap, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cap);
    Validate.notNull(sabrData);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = PRC.visit(cap, sabrData);
    final PresentValueSensitivity forwardDr = new PresentValueSensitivity(PRSC.visit(cap, sabrData));
    final double df = sabrData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getPaymentTime());
    final double dfDr = -cap.getPaymentTime() * df;
    final double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(cap.getFixingTime(), maturity, cap.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(new DoublesPair(cap.getPaymentTime(), dfDr));
    final Map<String, List<DoublesPair>> resultMap = new HashMap<String, List<DoublesPair>>();
    resultMap.put(cap.getFundingCurveName(), list);
    PresentValueSensitivity result = new PresentValueSensitivity(resultMap);
    result = result.multiply(bsAdjoint[0]);
    result = result.add(forwardDr.multiply(df * (bsAdjoint[1] + bsAdjoint[2] * volatilityAdjoint[1])));
    result = result.multiply(cap.getNotional() * cap.getPaymentYearFraction());
    return result;
  }

  /**
   * Computes the present value SABR sensitivity of a cap/floor in the SABR model.
   * @param cap The cap/floor.
   * @param sabrData The SABR data. The SABR function need to be the Hagan function.
   * @return The present value SABR sensitivity.
   */
  public PresentValueSABRSensitivityDataBundle presentValueSABRSensitivity(final CapFloorIbor cap, final SABRInterestRateDataBundle sabrData) {
    Validate.notNull(cap);
    Validate.notNull(sabrData);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(cap.getStrike(), cap.getFixingTime(), cap.isCap());
    final double forward = PRC.visit(cap, sabrData);
    final double df = sabrData.getCurve(cap.getFundingCurveName()).getDiscountFactor(cap.getPaymentTime());
    final double maturity = cap.getFixingPeriodEndTime() - cap.getFixingPeriodStartTime();
    final double[] volatilityAdjoint = sabrData.getSABRParameter().getVolatilityAdjoint(cap.getFixingTime(), maturity, cap.getStrike(), forward);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatilityAdjoint[0]);
    final double[] bsAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final DoublesPair expiryMaturity = new DoublesPair(cap.getFixingTime(), maturity);
    final PresentValueSABRSensitivityDataBundle sensi = new PresentValueSABRSensitivityDataBundle();
    sensi.addAlpha(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsAdjoint[2] * volatilityAdjoint[3]);
    sensi.addRho(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsAdjoint[2] * volatilityAdjoint[4]);
    sensi.addNu(expiryMaturity, cap.getNotional() * cap.getPaymentYearFraction() * df * bsAdjoint[2] * volatilityAdjoint[5]);
    return sensi;
  }

}
