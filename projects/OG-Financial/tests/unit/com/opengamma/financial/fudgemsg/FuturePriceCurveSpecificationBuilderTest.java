/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.volatility.surface.BloombergIRFuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveSpecification;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class FuturePriceCurveSpecificationBuilderTest extends FinancialTestBase {
  private static final String NAME = "SN";
  private static final Currency UID = Currency.USD;
  private static final FuturePriceCurveInstrumentProvider<?> PROVIDER = new BloombergIRFuturePriceCurveInstrumentProvider("ED", "Comdty", MarketDataRequirementNames.MARKET_VALUE);

  @Test
  public void testCycle() {
    final FuturePriceCurveSpecification specification = new FuturePriceCurveSpecification(NAME, UID, PROVIDER);
    assertEquals(specification, cycleObject(FuturePriceCurveSpecification.class, specification));
  }
}
