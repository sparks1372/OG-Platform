/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class AssetOrNothingOptionDefinitionTest {
  private static final double DELTA = 10;
  private static final double STRIKE = 100;
  private static final Expiry EXPIRY = new Expiry(DateUtil.getUTCDate(2010, 8, 1));
  private static final AssetOrNothingOptionDefinition CALL = new AssetOrNothingOptionDefinition(STRIKE, EXPIRY, true);
  private static final AssetOrNothingOptionDefinition PUT = new AssetOrNothingOptionDefinition(STRIKE, EXPIRY, false);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantYieldCurve(0.01), 0, new ConstantVolatilitySurface(0.1), STRIKE, DateUtil.getUTCDate(2010, 7, 1));

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    CALL.getPayoffFunction().getPayoff(null, null);
  }

  @Test
  public void testExerciseFunction() {
    StandardOptionDataBundle data = DATA;
    assertFalse(CALL.getExerciseFunction().shouldExercise(data, null));
    assertFalse(PUT.getExerciseFunction().shouldExercise(data, null));
    data = data.withSpot(STRIKE + DELTA);
    assertFalse(CALL.getExerciseFunction().shouldExercise(data, null));
    assertFalse(PUT.getExerciseFunction().shouldExercise(data, null));
  }

  @Test
  public void testPayoffFunction() {
    assertEquals(CALL.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    assertEquals(PUT.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    StandardOptionDataBundle data = DATA.withSpot(STRIKE + DELTA);
    assertEquals(CALL.getPayoffFunction().getPayoff(data, null), STRIKE + DELTA, 0);
    assertEquals(PUT.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE - DELTA);
    assertEquals(CALL.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(PUT.getPayoffFunction().getPayoff(data, null), STRIKE - DELTA, 0);
  }
}