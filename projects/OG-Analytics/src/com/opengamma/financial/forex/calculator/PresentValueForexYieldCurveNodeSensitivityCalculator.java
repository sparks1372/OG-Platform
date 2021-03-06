/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public final class PresentValueForexYieldCurveNodeSensitivityCalculator {
  private static final MatrixAlgebra MATRIX_ALGEBRA = new CommonsMatrixAlgebra(); //TODO make this a parameter
  private static final PresentValueForexYieldCurveNodeSensitivityCalculator INSTANCE = new PresentValueForexYieldCurveNodeSensitivityCalculator();
  private static final PresentValueNodeSensitivityCalculator NODE_SENSITIVITY_CALCULATOR = PresentValueNodeSensitivityCalculator.getDefaultInstance();

  public static PresentValueForexYieldCurveNodeSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  private PresentValueForexYieldCurveNodeSensitivityCalculator() {
  }

  public Map<String, DoubleMatrix1D> calculate(final ForexDerivative derivative, final Map<String, List<DoublesPair>> curveSensitivities, final Map<String, YieldAndDiscountCurve> interpolatedCurves,
      final Map<String, DoubleMatrix1D> couponSensitivities, final Map<String, DoubleMatrix2D> jacobians) {
    final Map<String, DoubleMatrix1D> results = new HashMap<String, DoubleMatrix1D>();
    for (final String curveName : interpolatedCurves.keySet()) {
      final DoubleMatrix1D nodeSensitivity = NODE_SENSITIVITY_CALCULATOR.curveToNodeSensitivities(curveSensitivities.get(curveName), interpolatedCurves.get(curveName));
      final int n = nodeSensitivity.getNumberOfElements();
      final DoubleMatrix2D inverseJacobian = MATRIX_ALGEBRA.getInverse(jacobians.get(curveName));
      final double[] result = new double[n];
      final DoubleMatrix1D couponSensitivity = couponSensitivities.get(curveName);
      for (int i = 0; i < n; i++) {
        double sum = 0;
        for (int j = 0; j < n; j++) {
          sum += -couponSensitivity.getEntry(i) * inverseJacobian.getEntry(j, i) * nodeSensitivity.getEntry(j);
        }
        result[i] = sum;
      }
      results.put(curveName, new DoubleMatrix1D(result));
    }
    return results;
  }
}
