/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;

/**
 * Gatheral's Stochastic Volatility Inspired (SVI) model
 */
public class SVIVolatilityFunction implements VolatilityFunctionProvider<SVIFormulaData> {

  @Override
  public Function1D<SVIFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option) {
    final double k = option.getK();
    return new Function1D<SVIFormulaData, Double>() {

      @Override
      public Double evaluate(final SVIFormulaData data) {
        final double a = data.getA();
        final double b = data.getB();
        final double rho = data.getRho();
        final double sigma = data.getSigma();
        final double m = data.getM();
        final double d = k - m;
        return a + b * (rho * d + Math.sqrt(d * d + sigma * sigma));
      }

    };
  }

}