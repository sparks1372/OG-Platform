/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math;

import static com.opengamma.math.number.ComplexNumber.I;

import org.apache.commons.lang.Validate;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */

public class TrigonometricFunctionUtils {
  private static final ComplexNumber NEGATIVE_I = new ComplexNumber(0, -1);

  public static double acos(final double x) {
    return Math.acos(x);
  }

  /**
   * arccos - the inverse of cos
   * @param z A complex number
   * @return acos(z)
   */
  public static ComplexNumber acos(final ComplexNumber z) {
    Validate.notNull(z, "z");
    return ComplexMathUtils.multiply(NEGATIVE_I, ComplexMathUtils.log(ComplexMathUtils.add(z, ComplexMathUtils.sqrt(ComplexMathUtils.subtract(ComplexMathUtils.multiply(z, z), 1)))));
  }

  public static double acosh(final double x) {
    final double y = x * x - 1;
    Validate.isTrue(y >= 0, "|x|>=1.0 for real solution");
    return Math.log(x + Math.sqrt(x * x - 1));
  }

  public static ComplexNumber acosh(final ComplexNumber z) {
    Validate.notNull(z, "z");
    return ComplexMathUtils.log(ComplexMathUtils.add(z, ComplexMathUtils.sqrt(ComplexMathUtils.subtract(ComplexMathUtils.multiply(z, z), 1))));
  }

  public static double asin(final double x) {

    return Math.asin(x);
  }

  public static ComplexNumber asin(final ComplexNumber z) {
    Validate.notNull(z, "z");
    return ComplexMathUtils.multiply(NEGATIVE_I,
        ComplexMathUtils.log(ComplexMathUtils.add(ComplexMathUtils.multiply(I, z), ComplexMathUtils.sqrt(ComplexMathUtils.subtract(1, ComplexMathUtils.multiply(z, z))))));
  }

  public static double asinh(final double x) {
    return Math.log(x + Math.sqrt(x * x + 1));
  }

  public static ComplexNumber asinh(final ComplexNumber z) {
    Validate.notNull(z, "z");
    return ComplexMathUtils.log(ComplexMathUtils.add(z, ComplexMathUtils.sqrt(ComplexMathUtils.add(ComplexMathUtils.multiply(z, z), 1))));
  }

  public static double atan(final double x) {
    return Math.atan(x);
  }

  public static ComplexNumber atan(final ComplexNumber z) {
    Validate.notNull(z, "z");
    final ComplexNumber iZ = ComplexMathUtils.multiply(z, I);
    final ComplexNumber half = new ComplexNumber(0, 0.5);
    return ComplexMathUtils.multiply(half, ComplexMathUtils.log(ComplexMathUtils.divide(ComplexMathUtils.subtract(1, iZ), ComplexMathUtils.add(1, iZ))));
  }

  public static double atanh(final double x) {
    return 0.5 * Math.log((1 + x) / (1 - x));
  }

  //TODO R White 21/07/2011 not sure why this was used over the equivalent below 
//  public static ComplexNumber atanh(final ComplexNumber z) {
//    Validate.notNull(z, "z");
//    return ComplexMathUtils.log(ComplexMathUtils.divide(ComplexMathUtils.sqrt(ComplexMathUtils.subtract(1, ComplexMathUtils.multiply(z, z))), ComplexMathUtils.subtract(1, z)));
//  }

  public static ComplexNumber atanh(final ComplexNumber z) {
    Validate.notNull(z, "z");
    return ComplexMathUtils.multiply(0.5, ComplexMathUtils.log(ComplexMathUtils.divide(ComplexMathUtils.add(1, z), ComplexMathUtils.subtract(1, z))));
  }

  public static double cos(final double x) {
    return Math.cos(x);
  }

  public static ComplexNumber cos(final ComplexNumber z) {
    Validate.notNull(z, "z");
    final double x = z.getReal();
    final double y = z.getImaginary();
    return new ComplexNumber(Math.cos(x) * Math.cosh(y), -Math.sin(x) * Math.sinh(y));
  }

  public static double cosh(final double x) {
    return Math.cosh(x);
  }

  public static ComplexNumber cosh(final ComplexNumber z) {
    Validate.notNull(z, "z");
    return new ComplexNumber(Math.cosh(z.getReal()) * Math.cos(z.getImaginary()), Math.sinh(z.getReal()) * Math.sin(z.getImaginary()));
  }

  public static double sin(final double x) {
    return Math.sin(x);
  }

  public static ComplexNumber sin(final ComplexNumber z) {
    Validate.notNull(z, "z");
    final double x = z.getReal();
    final double y = z.getImaginary();
    return new ComplexNumber(Math.sin(x) * Math.cosh(y), Math.cos(x) * Math.sinh(y));
  }

  public static double sinh(final double x) {
    return Math.sinh(x);
  }

  public static ComplexNumber sinh(final ComplexNumber z) {
    Validate.notNull(z, "z");
    return new ComplexNumber(Math.sinh(z.getReal()) * Math.cos(z.getImaginary()), Math.cosh(z.getReal()) * Math.sin(z.getImaginary()));
  }

  public static double tan(final double x) {
    return Math.tan(x);
  }

  public static ComplexNumber tan(final ComplexNumber z) {
    final ComplexNumber b = ComplexMathUtils.exp(ComplexMathUtils.multiply(ComplexMathUtils.multiply(I, 2), z));
    return ComplexMathUtils.divide(ComplexMathUtils.subtract(b, 1), ComplexMathUtils.multiply(I, ComplexMathUtils.add(b, 1)));
  }

  public static double tanh(final double x) {
    return Math.tanh(x);
  }

  public static ComplexNumber tanh(final ComplexNumber z) {
    final ComplexNumber z2 = ComplexMathUtils.exp(z);
    final ComplexNumber z3 = ComplexMathUtils.exp(ComplexMathUtils.multiply(z, -1));
    return ComplexMathUtils.divide(ComplexMathUtils.subtract(z2, z3), ComplexMathUtils.add(z2, z3));
  }

}
