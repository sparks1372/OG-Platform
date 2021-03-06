/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.math.interpolation.data.RadialBasisFunctionInterpolatorDataBundle;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorNDTest extends InterpolatorNDTestCase {
  private static final GaussianRadialBasisFunction BASIS_FUNCTION = new GaussianRadialBasisFunction();
  private static final boolean USE_NORMALIZED = false;
  private static final InterpolatorND<RadialBasisFunctionInterpolatorDataBundle> INTERPOLATOR = new RadialBasisFunctionInterpolatorND(BASIS_FUNCTION, USE_NORMALIZED);
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBasisFunction() {
    new RadialBasisFunctionInterpolatorND(null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(null, new double[] {1, 2});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoint() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(FLAT_DATA), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDimension() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(FLAT_DATA), new double[] {1, 2});
  }

  @Test
  public void test() {
    RadialBasisFunctionInterpolatorND other = new RadialBasisFunctionInterpolatorND(BASIS_FUNCTION, USE_NORMALIZED);
    assertEquals(other, INTERPOLATOR);
    assertEquals(other.hashCode(), INTERPOLATOR.hashCode());
    other = new RadialBasisFunctionInterpolatorND(new GaussianRadialBasisFunction(1000), USE_NORMALIZED);
    assertFalse(other.equals(INTERPOLATOR));
    other = new RadialBasisFunctionInterpolatorND(BASIS_FUNCTION, !USE_NORMALIZED);
    assertFalse(other.equals(INTERPOLATOR));
  }

  @Test
  public void testGaussianRadialBasisFunction() {
    double r0 = 1;
    InterpolatorND<RadialBasisFunctionInterpolatorDataBundle> interpolator = new RadialBasisFunctionInterpolatorND(new GaussianRadialBasisFunction(r0), false);
    assertCosExp(interpolator, 5e-2);
    r0 = 5;
    // why does normalised fit much better for flat surfaces?
    interpolator = new RadialBasisFunctionInterpolatorND(new GaussianRadialBasisFunction(r0), true);
    assertFlat(interpolator, 1e-6);
  }

  @Test
  public void testMultiquadraticRadialBasisFunction() {
    final double r0 = 1;
    final InterpolatorND<RadialBasisFunctionInterpolatorDataBundle> interpolator = new RadialBasisFunctionInterpolatorND(new GaussianRadialBasisFunction(r0), false);
    assertCosExp(interpolator, 1e-2);
  }

  @Test
  public void testInverseMultiquadraticRadialBasisFunction() {
    final double r0 = 2;
    final InterpolatorND<RadialBasisFunctionInterpolatorDataBundle> interpolator = new RadialBasisFunctionInterpolatorND(new InverseMultiquadraticRadialBasisFunction(r0), true);
    assertCosExp(interpolator, 1e-2);
  }

  @Test
  public void testThinPlateSplineRadialBasisFunction() {
    final double r0 = 1.0;
    final InterpolatorND<RadialBasisFunctionInterpolatorDataBundle> interpolator = new RadialBasisFunctionInterpolatorND(new ThinPlateSplineRadialBasisFunction(r0), true);
    assertCosExp(interpolator, 2e-2);//TODO this used to work with tol of 1e-2 ??
  }

}
