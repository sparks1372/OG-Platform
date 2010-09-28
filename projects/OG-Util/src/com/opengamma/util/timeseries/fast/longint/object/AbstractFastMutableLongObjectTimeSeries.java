/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint.object;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;


/**
 * @author jim
 *         Contains methods to make Primitive time series work with the normal
 *         non-primitive time series interface (where possible)
 */
public abstract class AbstractFastMutableLongObjectTimeSeries<T> extends AbstractFastLongObjectTimeSeries<T> implements FastMutableLongObjectTimeSeries<T> {

  protected AbstractFastMutableLongObjectTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
  }

  public void putDataPoint(final Long time, final T value) {
    primitivePutDataPoint(time, value);
  }

  public void removeDataPoint(final Long time) {
    primitiveRemoveDataPoint(time);
  }

}