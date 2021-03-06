/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.interestrate.InterestRateDerivative;

/**
 * 
 * @param <T> Type of the InterestRateDerivative that the definition can be converted to
 */
public interface FixedIncomeFutureInstrumentDefinition<T extends InterestRateDerivative> {

  T toDerivative(ZonedDateTime date, double price, String... yieldCurveNames);

  <U, V> V accept(FixedIncomeFutureInstrumentDefinitionVisitor<U, V> visitor, U data);

  <V> V accept(FixedIncomeFutureInstrumentDefinitionVisitor<?, V> visitor);

}
