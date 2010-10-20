/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class NodalCurveShiftFunction implements CurveShiftFunction<NodalDoublesCurve> {

  @Override
  public NodalDoublesCurve evaluate(final NodalDoublesCurve curve, final double shift) {
    Validate.notNull(curve, "curve");
    return evaluate(curve, shift, "PARALLEL_SHIFT_" + curve.getName());
  }

  @Override
  public NodalDoublesCurve evaluate(final NodalDoublesCurve curve, final double shift, final String newName) {
    Validate.notNull(curve, "curve");
    final double[] xData = curve.getXDataAsPrimitive();
    final double[] yData = curve.getYDataAsPrimitive();
    final double[] shiftedY = new double[yData.length];
    int i = 0;
    for (final double y : yData) {
      shiftedY[i++] = y + shift;
    }
    return NodalDoublesCurve.fromSorted(xData, shiftedY, newName);
  }

  @Override
  public NodalDoublesCurve evaluate(final NodalDoublesCurve curve, final double x, final double shift) {
    Validate.notNull(curve, "curve");
    return evaluate(curve, x, shift, "SINGLE_SHIFT_" + curve.getName());
  }

  @Override
  public NodalDoublesCurve evaluate(final NodalDoublesCurve curve, final double x, final double shift, final String newName) {
    Validate.notNull(curve, "curve");
    final double[] xData = curve.getXDataAsPrimitive();
    final int index = Arrays.binarySearch(xData, x);
    if (index < 0) {
      throw new IllegalArgumentException("Curve does not contain data for x point " + x);
    }
    final double[] yData = curve.getYDataAsPrimitive();
    final double[] shiftedY = Arrays.copyOf(yData, yData.length);
    shiftedY[index] += shift;
    return NodalDoublesCurve.fromSorted(xData, shiftedY, newName);
  }

  @Override
  public NodalDoublesCurve evaluate(final NodalDoublesCurve curve, final double[] xShift, final double[] yShift) {
    Validate.notNull(curve, "curve");
    return evaluate(curve, xShift, yShift, "MULTIPLE_POINT_SHIFT_" + curve.getName());
  }

  @Override
  public NodalDoublesCurve evaluate(final NodalDoublesCurve curve, final double[] xShift, final double[] yShift, final String newName) {
    Validate.notNull(curve, "curve");
    Validate.isTrue(xShift.length == yShift.length);
    final double[] xData = curve.getXDataAsPrimitive();
    final double[] yData = curve.getYDataAsPrimitive();
    final double[] shiftedY = Arrays.copyOf(yData, yData.length);
    for (int i = 0; i < xShift.length; i++) {
      final int index = Arrays.binarySearch(xData, xShift[i]);
      if (index < 0) {
        throw new IllegalArgumentException("Curve does not contain data for x = " + xShift[i]);
      }
      shiftedY[index] += yShift[i];
    }
    return NodalDoublesCurve.fromSorted(xData, shiftedY, newName);
  }

}