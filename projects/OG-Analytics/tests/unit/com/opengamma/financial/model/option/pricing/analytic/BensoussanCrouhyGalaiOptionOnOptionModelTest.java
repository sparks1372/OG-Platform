/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.option.definition.EuropeanOptionOnEuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class BensoussanCrouhyGalaiOptionOnOptionModelTest {
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final double SPOT = 500;
  private static final double UNDERLYING_STRIKE = 520;
  private static final Expiry UNDERLYING_EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final EuropeanVanillaOptionDefinition UNDERLYING = new EuropeanVanillaOptionDefinition(UNDERLYING_STRIKE, UNDERLYING_EXPIRY, true);
  private static final double STRIKE = 50;
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final EuropeanOptionOnEuropeanVanillaOptionDefinition OPTION = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, UNDERLYING);
  private static final BensoussanCrouhyGalaiOptionOnOptionModel BCG = new BensoussanCrouhyGalaiOptionOnOptionModel();
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantYieldCurve(0.08), 0.05, new ConstantVolatilitySurface(0.35), SPOT, DATE);
  private static final EuropeanOptionOnEuropeanVanillaOptionModel MODEL = new EuropeanOptionOnEuropeanVanillaOptionModel();

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    BCG.getPricingFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    BCG.getPricingFunction(OPTION).evaluate((StandardOptionDataBundle) null);
  }

  @Test
  public void test() {
    assertEquals(BCG.getPricingFunction(OPTION).evaluate(DATA), 19.9147, 1e-4);
    final EuropeanVanillaOptionDefinition underlying = new EuropeanVanillaOptionDefinition(SPOT - 100, UNDERLYING_EXPIRY, true);
    final EuropeanOptionOnEuropeanVanillaOptionDefinition option = new EuropeanOptionOnEuropeanVanillaOptionDefinition(20, EXPIRY, true, underlying);
    assertEquals(BCG.getPricingFunction(option).evaluate(DATA) / MODEL.getPricingFunction(option).evaluate(DATA), 1, 1e-2);
  }
}