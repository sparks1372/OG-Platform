/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.analytic.formula.SABRFormulaData;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 *  This is the form given in Obloj, Fine-Tune Your Simle (2008), and supposedly corresponds to that given in Berestycki, 
 *  Computing the implied volatility in stochastic volatility models (2004). However appears to be the same as Hagan's 
 */
public class SABRBerestyckiVolatilityFunction implements VolatilityFunctionProvider<SABRFormulaData> {
  private static final double EPS = 1e-15;

  @Override
  public Function1D<SABRFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option) {
    final double k = option.getK();
    final double t = option.getT();
    return new Function1D<SABRFormulaData, Double>() {

      @Override
      public final Double evaluate(final SABRFormulaData data) {
        final double alpha = data.getAlpha();
        final double beta = data.getBeta();
        final double rho = data.getRho();
        final double nu = data.getNu();
        final double f = data.getF();
        double i0;
        final double beta1 = 1 - beta;

        if (CompareUtils.closeEquals(f, k, EPS)) {
          i0 = alpha / Math.pow(k, beta1);
        } else {

          final double x = Math.log(f / k);

          if (CompareUtils.closeEquals(nu, 0, EPS)) {
            if (CompareUtils.closeEquals(beta, 1.0, EPS)) {
              return alpha; // this is just log-normal
            }
            i0 = x * alpha * beta1 / (Math.pow(f, beta1) - Math.pow(k, beta1));
          } else {

            double z;
            if (beta == 1.0) {
              z = nu * x / alpha;

            } else {
              z = nu * (Math.pow(f, beta1) - Math.pow(k, beta1)) / alpha / beta1;
            }
            final double temp = (Math.sqrt(1 + z * (z - 2 * rho)) + z - rho) / (1 - rho);
            i0 = nu * x / Math.log(temp);

          }
        }

        final double f1sqrt = Math.pow(f * k, beta1 / 2);
        final double i1 = beta1 * beta1 * alpha * alpha / 24 / f1sqrt / f1sqrt + rho * alpha * beta * nu / 4 / f1sqrt + nu * nu * (2 - 3 * rho * rho) / 24;

        return i0 * (1 + i1 * t);
      }
    };
  }

}