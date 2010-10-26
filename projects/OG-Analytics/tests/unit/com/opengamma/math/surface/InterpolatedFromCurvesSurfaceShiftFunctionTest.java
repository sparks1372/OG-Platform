/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.CurveShiftFunctionFactory;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.curve.NodalDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 */
public class InterpolatedFromCurvesSurfaceShiftFunctionTest {
  private static final NodalDoublesCurve NODAL = NodalDoublesCurve.fromSorted(new double[] {1, 2, 3, 4}, new double[] {2, 3, 4, 5}, "Q");
  private static final LinearInterpolator1D INTERPOLATOR = new LinearInterpolator1D();
  private static final InterpolatedDoublesCurve INTERPOLATED1 = InterpolatedDoublesCurve.fromSorted(new double[] {1, 2, 3, 4}, new double[] {3, 4, 5, 6}, INTERPOLATOR, "W");
  private static final InterpolatedDoublesCurve INTERPOLATED2 = InterpolatedDoublesCurve.fromSorted(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7}, INTERPOLATOR, "E");
  private static final double[] POINTS = new double[] {1, 2, 4};
  @SuppressWarnings("unchecked")
  private static final Curve<Double, Double>[] CURVES = new Curve[] {NODAL, INTERPOLATED1, INTERPOLATED2};
  private static final String NAME = "D";
  private static final InterpolatedFromCurvesDoublesSurface SURFACE1 = InterpolatedFromCurvesDoublesSurface.fromSorted(true, POINTS, CURVES, INTERPOLATOR, NAME);
  private static final InterpolatedFromCurvesDoublesSurface SURFACE2 = InterpolatedFromCurvesDoublesSurface.fromSorted(false, POINTS, CURVES, INTERPOLATOR, NAME);
  private static final InterpolatedFromCurvesSurfaceShiftFunction F = new InterpolatedFromCurvesSurfaceShiftFunction();

  @Test(expected = IllegalArgumentException.class)
  public void testNull1() {
    F.evaluate(null, 3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull2() {
    F.evaluate(null, 3, NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull3() {
    F.evaluate(null, 3, 4, 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull4() {
    F.evaluate(null, 3, 4, 5, NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull5() {
    F.evaluate(null, new double[] {3}, new double[] {4}, new double[] {5});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull6() {
    F.evaluate(null, new double[] {3}, new double[] {4}, new double[] {5}, NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthY1() {
    F.evaluate(SURFACE1, new double[] {1}, new double[] {2, 3}, new double[] {4});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthY2() {
    F.evaluate(SURFACE1, new double[] {1}, new double[] {2, 3}, new double[] {4}, "M");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthZ1() {
    F.evaluate(SURFACE1, new double[] {1}, new double[] {2}, new double[] {3, 4});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLengthZ2() {
    F.evaluate(SURFACE1, new double[] {1}, new double[] {2}, new double[] {3, 4}, "L");
  }

  @Test
  public void testParallel() {
    final double shift = 0.54;
    InterpolatedFromCurvesDoublesSurface surface = F.evaluate(SURFACE1, shift);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertTrue(surface.isXZCurves());
    Curve<Double, Double>[] curves = surface.getCurves();
    Curve<Double, Double> shifted = curves[0];
    testParallelShiftedCurves(shift, shifted, NODAL, NodalDoublesCurve.class);
    shifted = curves[1];
    testParallelShiftedCurves(shift, shifted, INTERPOLATED1, InterpolatedDoublesCurve.class);
    shifted = curves[2];
    testParallelShiftedCurves(shift, shifted, INTERPOLATED2, InterpolatedDoublesCurve.class);
    assertEquals(surface.getName(), "PARALLEL_SHIFT_" + NAME);
    final String newName = "T";
    surface = F.evaluate(SURFACE1, shift, newName);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertTrue(surface.isXZCurves());
    curves = surface.getCurves();
    shifted = curves[0];
    testParallelShiftedCurves(shift, shifted, NODAL, NodalDoublesCurve.class);
    shifted = curves[1];
    testParallelShiftedCurves(shift, shifted, INTERPOLATED1, InterpolatedDoublesCurve.class);
    shifted = curves[2];
    testParallelShiftedCurves(shift, shifted, INTERPOLATED2, InterpolatedDoublesCurve.class);
    assertEquals(surface.getName(), newName);
    surface = F.evaluate(SURFACE1, shift);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertTrue(surface.isXZCurves());
    curves = surface.getCurves();
    shifted = curves[0];
    testParallelShiftedCurves(shift, shifted, NODAL, NodalDoublesCurve.class);
    shifted = curves[1];
    testParallelShiftedCurves(shift, shifted, INTERPOLATED1, InterpolatedDoublesCurve.class);
    shifted = curves[2];
    testParallelShiftedCurves(shift, shifted, INTERPOLATED2, InterpolatedDoublesCurve.class);
    assertEquals(surface.getName(), "PARALLEL_SHIFT_" + NAME);
    surface = F.evaluate(SURFACE1, shift, newName);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertTrue(surface.isXZCurves());
    curves = surface.getCurves();
    shifted = curves[0];
    testParallelShiftedCurves(shift, shifted, NODAL, NodalDoublesCurve.class);
    shifted = curves[1];
    testParallelShiftedCurves(shift, shifted, INTERPOLATED1, InterpolatedDoublesCurve.class);
    shifted = curves[2];
    testParallelShiftedCurves(shift, shifted, INTERPOLATED2, InterpolatedDoublesCurve.class);
    assertEquals(surface.getName(), newName);
  }

  private void testParallelShiftedCurves(final double shift, final Curve<Double, Double> shifted, final Curve<Double, Double> original, final Class<?> clazz) {
    Double[] yData1;
    Double[] yData2;
    assertEquals(shifted.getClass(), clazz);
    assertArrayEquals(shifted.getXData(), original.getXData());
    yData1 = shifted.getYData();
    yData2 = original.getYData();
    for (int i = 0; i < yData1.length; i++) {
      assertEquals(yData1[i], yData2[i] + shift, 0);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSingleShiftNoX() {
    F.evaluate(SURFACE2, 1.2, 2, 0.3);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSingleShiftNoY() {
    F.evaluate(SURFACE1, 1, 1.2, 0.2);
  }

  @Test
  public void testSingleShiftXZ() {
    final double x = 1;
    final double y = 4;
    final double shift = 0.54;
    InterpolatedFromCurvesDoublesSurface surface = F.evaluate(SURFACE1, x, y, shift);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertTrue(surface.isXZCurves());
    Curve<Double, Double>[] curves = surface.getCurves();
    Curve<Double, Double> shifted = curves[0];
    assertCurveEquals(shifted, NODAL, false);
    shifted = curves[1];
    assertCurveEquals(shifted, INTERPOLATED1, false);
    shifted = curves[2];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED2, x, shift), false);
    assertEquals(surface.getName(), "SINGLE_SHIFT_" + NAME);
    final String newName = "K";
    surface = F.evaluate(SURFACE1, x, y, shift, newName);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertTrue(surface.isXZCurves());
    curves = surface.getCurves();
    shifted = curves[0];
    assertCurveEquals(shifted, NODAL, false);
    shifted = curves[1];
    assertCurveEquals(shifted, INTERPOLATED1, false);
    shifted = curves[2];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED2, x, shift), false);
    assertEquals(surface.getName(), newName);
  }

  @Test
  public void testSingleShiftYZ() {
    final double x = 1;
    final double y = 4;
    final double shift = 0.54;
    InterpolatedFromCurvesDoublesSurface surface = F.evaluate(SURFACE2, x, y, shift);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertFalse(surface.isXZCurves());
    Curve<Double, Double>[] curves = surface.getCurves();
    Curve<Double, Double> shifted = curves[0];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(NODAL, y, shift), false);
    shifted = curves[1];
    assertCurveEquals(shifted, INTERPOLATED1, false);
    shifted = curves[2];
    assertCurveEquals(shifted, INTERPOLATED2, false);
    assertEquals(surface.getName(), "SINGLE_SHIFT_" + NAME);
    final String newName = "K";
    surface = F.evaluate(SURFACE2, x, y, shift, newName);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertFalse(surface.isXZCurves());
    curves = surface.getCurves();
    shifted = curves[0];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(NODAL, y, shift), false);
    shifted = curves[1];
    assertCurveEquals(shifted, INTERPOLATED1, false);
    shifted = curves[2];
    assertCurveEquals(shifted, INTERPOLATED2, false);
    assertEquals(surface.getName(), newName);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMultipleShiftNoX() {
    F.evaluate(SURFACE2, new double[] {1, 1.2}, new double[] {1, 2}, new double[] {0.3, 0.3});
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMultipleShiftShiftNoY() {
    F.evaluate(SURFACE1, new double[] {1, 2}, new double[] {1, 1.2}, new double[] {0.3, 0.3});
  }

  @Test
  public void testMultipleShiftNoData() {
    final double[] xShift = new double[0];
    final double[] yShift = new double[0];
    final double[] shift = new double[0];
    InterpolatedFromCurvesDoublesSurface surface = F.evaluate(SURFACE1, xShift, yShift, shift);
    assertArrayEquals(surface.getCurves(), CURVES);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertTrue(surface.isXZCurves());
    assertEquals(surface.getName(), "MULTIPLE_SHIFT_" + NAME);
    surface = F.evaluate(SURFACE1, xShift, yShift, shift, "A");
    assertArrayEquals(surface.getCurves(), CURVES);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertTrue(surface.isXZCurves());
    assertEquals(surface.getName(), "A");
    surface = F.evaluate(SURFACE2, xShift, yShift, shift);
    assertArrayEquals(surface.getCurves(), CURVES);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertFalse(surface.isXZCurves());
    assertEquals(surface.getName(), "MULTIPLE_SHIFT_" + NAME);
    surface = F.evaluate(SURFACE2, xShift, yShift, shift, "A");
    assertArrayEquals(surface.getCurves(), CURVES);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertFalse(surface.isXZCurves());
    assertEquals(surface.getName(), "A");
  }

  @Test
  public void testMultipleShiftXZ() {
    final double[] x = new double[] {1, 2};
    final double[] y = new double[] {4, 2};
    final double[] shift = new double[] {0.54, -0.9};
    InterpolatedFromCurvesDoublesSurface surface = F.evaluate(SURFACE1, x, y, shift);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertTrue(surface.isXZCurves());
    Curve<Double, Double>[] curves = surface.getCurves();
    Curve<Double, Double> shifted = curves[0];
    assertCurveEquals(shifted, NODAL, false);
    shifted = curves[1];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED1, x[1], shift[1]), false);
    shifted = curves[2];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED2, x[0], shift[0]), false);
    assertEquals(surface.getName(), "MULTIPLE_SHIFT_" + NAME);
    final String newName = "K";
    surface = F.evaluate(SURFACE1, x, y, shift, newName);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertTrue(surface.isXZCurves());
    curves = surface.getCurves();
    shifted = curves[0];
    assertCurveEquals(shifted, NODAL, false);
    shifted = curves[1];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED1, x[1], shift[1]), false);
    shifted = curves[2];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED2, x[0], shift[0]), false);
    assertEquals(surface.getName(), newName);
  }

  @Test
  public void testMultipleShiftYZ() {
    final double[] x = new double[] {1, 2};
    final double[] y = new double[] {4, 2};
    final double[] shift = new double[] {0.54, -0.9};
    InterpolatedFromCurvesDoublesSurface surface = F.evaluate(SURFACE2, x, y, shift);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertFalse(surface.isXZCurves());
    Curve<Double, Double>[] curves = surface.getCurves();
    Curve<Double, Double> shifted = curves[0];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(NODAL, y[0], shift[0]), false);
    shifted = curves[1];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED1, y[1], shift[1]), false);
    shifted = curves[2];
    assertCurveEquals(shifted, INTERPOLATED2, false);
    assertEquals(surface.getName(), "MULTIPLE_SHIFT_" + NAME);
    final String newName = "K";
    surface = F.evaluate(SURFACE2, x, y, shift, newName);
    assertEquals(surface.getInterpolator(), INTERPOLATOR);
    assertArrayEquals(surface.getPoints(), POINTS, 0);
    assertFalse(surface.isXZCurves());
    curves = surface.getCurves();
    shifted = curves[0];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(NODAL, y[0], shift[0]), false);
    shifted = curves[1];
    assertCurveEquals(shifted, CurveShiftFunctionFactory.getShiftedCurve(INTERPOLATED1, y[1], shift[1]), false);
    shifted = curves[2];
    assertCurveEquals(shifted, INTERPOLATED2, false);
    assertEquals(surface.getName(), newName);
  }

  private void assertCurveEquals(final Curve<Double, Double> c1, final Curve<Double, Double> c2, final boolean testName) {
    assertEquals(c1.getClass(), c2.getClass());
    assertArrayEquals(c1.getXData(), c2.getXData());
    assertArrayEquals(c1.getYData(), c2.getYData());
    if (c1 instanceof InterpolatedDoublesCurve) {
      assertEquals(((InterpolatedDoublesCurve) c1).getInterpolator(), ((InterpolatedDoublesCurve) c2).getInterpolator());
    }
    if (testName) {
      assertEquals(c1.getName(), c2.getName());
    }
  }
}