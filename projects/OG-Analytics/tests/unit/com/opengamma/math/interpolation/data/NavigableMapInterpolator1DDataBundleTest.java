/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.data;

import org.testng.annotations.Test;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.data.NavigableMapInterpolator1DDataBundle;

/**
 * 
 */
public class NavigableMapInterpolator1DDataBundleTest extends Interpolator1DDataBundleTestCase {

  @Override
  protected Interpolator1DDataBundle createDataBundle(double[] keys, double[] values) {
    NavigableMap<Double, Double> map = new TreeMap<Double, Double>();
    for (int i = 0; i < keys.length; i++) {
      map.put(keys[i], values[i]);
    }
    return new NavigableMapInterpolator1DDataBundle(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullInputs() {
    new NavigableMapInterpolator1DDataBundle(null);
  }

}
