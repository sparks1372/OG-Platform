/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFormula;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedVolFormula;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.minimization.TransformParameters;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 */
public class HestonFitter {

  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare();
  private static final FFTPricer FFT_PRICER = new FFTPricer();
  private static final FourierPricer FOURIER_PRICER = new FourierPricer();
  // private static final Interpolator1D<Interpolator1DDataBundle> INTERPOLATOR = Interpolator1DFactory.getInterpolator("NaturalCubicSpline");
  private static final Interpolator1D<Interpolator1DDataBundle> INTERPOLATOR = Interpolator1DFactory.getInterpolator("DoubleQuadratic");

  private static final int N_PARAMETERS = 5;
  private static final ParameterLimitsTransform[] TRANSFORMS;

  static {
    TRANSFORMS = new ParameterLimitsTransform[N_PARAMETERS];
    // TRANSFORMS[0] = new NullTransform();
    // TRANSFORMS[1] = new NullTransform();
    // TRANSFORMS[2] = new NullTransform();
    // TRANSFORMS[3] = new NullTransform();
    // TRANSFORMS[4] = new NullTransform();
    TRANSFORMS[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // kappa > 0
    TRANSFORMS[1] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // theta > 0
    TRANSFORMS[2] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // vol0 > 0
    TRANSFORMS[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // omega > 0
    TRANSFORMS[4] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
  }

  public LeastSquareResults solve(final double forward, final double maturity, final double[] strikes, final double[] blackVols, final double[] errors, final double[] initialValues, final BitSet fixed) {

    final int n = strikes.length;
    Validate.isTrue(n == blackVols.length, "strikes and vols must be same length");
    Validate.isTrue(n == errors.length, "errors and vols must be same length");

    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);

    // // double moneynessRange = Math.max(-Math.log(strikes[0] / forward), Math.log(strikes[strikes.length - 1] / forward));
    // final double maxDeltaMoneyness = 0.4; // moneynessRange / strikes.length;
    // final int nStrikes = strikes.length;
    final double alpha = -0.5;
    final double tol = 1e-8;
    final double limitSigma = (blackVols[0] + blackVols[blackVols.length - 1]) / 2.0;
    final double sL = strikes[0];
    final double sH = strikes[n - 1];

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> hestonVols = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D fp) {
        final DoubleMatrix1D mp = transforms.inverseTransform(fp);
        final double kappa = mp.getEntry(0);
        final double theta = mp.getEntry(1);
        final double vol0 = mp.getEntry(2);
        final double omega = mp.getEntry(3);
        final double rho = mp.getEntry(4);
        final CharacteristicExponent ce = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho, maturity);
        final double[][] strikeNPrice = FFT_PRICER.price(forward, 1.0, true, ce, sL, sH, n, alpha, tol, limitSigma);
        final int nStrikes = strikeNPrice.length;
        final double[] k = new double[nStrikes];
        final double[] vol = new double[nStrikes];
        for (int i = 0; i < nStrikes; i++) {
          k[i] = strikeNPrice[i][0];
          try {
            vol[i] = BlackImpliedVolFormula.impliedVolNewton(strikeNPrice[i][1], forward, k[i], 1.0, maturity, true);
          } catch (final Exception e) {
            vol[i] = 0.0;
          }
        }

        final Interpolator1DDataBundle dataBundle = INTERPOLATOR.getDataBundleFromSortedArrays(k, vol);
        final double[] res = new double[n];
        for (int i = 0; i < n; i++) {
          res[i] = INTERPOLATOR.interpolate(dataBundle, strikes[i]);
        }
        return new DoubleMatrix1D(res);

      }
    };

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));

    final LeastSquareResults results = SOLVER.solve(new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), hestonVols, fp);
    return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getParameters()), new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }

  public LeastSquareResults solvePrice(final double forward, final double maturity, final double[] strikes, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitSet fixed) {

    final int n = strikes.length;
    Validate.isTrue(n == blackVols.length, "strikes and vols must be same length");
    Validate.isTrue(n == errors.length, "errors and vols must be same length");

    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);
    final double sL = strikes[0];
    final double sH = strikes[n - 1];

    final double alpha = -0.5;
    final double tol = 1e-8;
    final double[] prices = new double[blackVols.length];
    for (int i = 0; i < blackVols.length; i++) {
      prices[i] = BlackFormula.optionPrice(forward, strikes[i], 1.0, blackVols[i], maturity, true);
    }
    final double limitSigma = (blackVols[0] + blackVols[blackVols.length - 1]) / 2.0;

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> hestonVols = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D fp) {
        final DoubleMatrix1D mp = transforms.inverseTransform(fp);
        final double kappa = mp.getEntry(0);
        final double theta = mp.getEntry(1);
        final double vol0 = mp.getEntry(2);
        final double omega = mp.getEntry(3);
        final double rho = mp.getEntry(4);
        final CharacteristicExponent ce = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho, maturity);
        final double[][] strikeNPrice = FFT_PRICER.price(forward, 1.0, true, ce, sL, sH, n, alpha, tol, limitSigma);
        final int nStrikes = strikeNPrice.length;
        final double[] k = new double[nStrikes];
        final double[] price = new double[nStrikes];
        for (int i = 0; i < nStrikes; i++) {
          k[i] = strikeNPrice[i][0];
          price[i] = strikeNPrice[i][1];
        }
        final Interpolator1DDataBundle dataBundle = INTERPOLATOR.getDataBundle(k, price);
        final int n = strikes.length;
        final double[] res = new double[n];
        for (int i = 0; i < n; i++) {
          res[i] = INTERPOLATOR.interpolate(dataBundle, strikes[i]);
        }
        return new DoubleMatrix1D(res);
      }
    };

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));

    final LeastSquareResults results = SOLVER.solve(new DoubleMatrix1D(prices), new DoubleMatrix1D(errors), hestonVols, fp);
    return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getParameters()), new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }

  public LeastSquareResults solveFourierIntegral(final double forward, final double maturity, final double[] strikes, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitSet fixed) {

    final int n = strikes.length;
    Validate.isTrue(n == blackVols.length, "strikes and vols must be same length");
    Validate.isTrue(n == errors.length, "errors and vols must be same length");

    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);

    final double alpha = -0.5;
    final double tol = 1e-8;
    final double limitSigma = (blackVols[0] + blackVols[blackVols.length - 1]) / 2.0;

    final ParameterizedFunction<Double, DoubleMatrix1D, Double> function = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike, final DoubleMatrix1D fp) {
        final DoubleMatrix1D mp = transforms.inverseTransform(fp);
        final double kappa = mp.getEntry(0);
        final double theta = mp.getEntry(1);
        final double vol0 = mp.getEntry(2);
        final double omega = mp.getEntry(3);
        final double rho = mp.getEntry(4);
        final CharacteristicExponent ce = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho, maturity);

        final double price = FOURIER_PRICER.price(forward, strike, 1.0, true, ce, alpha, tol, limitSigma);

        final double vol = BlackImpliedVolFormula.impliedVolNewton(price, forward, strike, 1.0, maturity, true);

        return vol;
      }
    };

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));

    //return SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), function, fp);

    final LeastSquareResults results = SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), function, fp);
    return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getParameters()), new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));

  }

}