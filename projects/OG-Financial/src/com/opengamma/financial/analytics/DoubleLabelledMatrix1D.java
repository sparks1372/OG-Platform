/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class DoubleLabelledMatrix1D extends LabelledMatrix1D<Double, Double> {
  private static final double TOLERANCE = 1e-15;

  public DoubleLabelledMatrix1D(final Double[] keys, final double[] values) {
    super(keys, values, TOLERANCE);
  }

  public DoubleLabelledMatrix1D(final Double[] keys, final Object[] labels, final double[] values) {
    super(keys, labels, values, TOLERANCE);
  }

  @Override
  public int compare(final Double d1, final Double d2, final Double tolerance) {
    return CompareUtils.compareWithTolerance(d1, d2, tolerance);
  }

  @Override
  protected LabelledMatrix1D<Double, Double> getMatrix(final Double[] keys, final Object[] labels, final double[] values) {
    return new DoubleLabelledMatrix1D(keys, labels, values);
  }

  @Override
  protected LabelledMatrix1D<Double, Double> getMatrix(final Double[] keys, final double[] values) {
    return new DoubleLabelledMatrix1D(keys, values);
  }
}
