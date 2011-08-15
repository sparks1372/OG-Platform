/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.market;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * A market bundle decorated for a given forward curve and a specific time. The (zero-coupon) rate is shifted by the shift provided.
 */
public class MarketForwardTimeDecorated extends MarketBundle {
  private final IborIndex _index;
  private final YieldAndDiscountCurve _forwardCurve;
  private final double _time;
  private final double _shift;

  /**
   * Constructor from an exiting market, the currency and a time to be decorated for discounting.
   * @param market The original market.
   * @param index The Ibor index.
   * @param time The time.
   * @param shift The shift.
   */
  public MarketForwardTimeDecorated(MarketBundle market, IborIndex index, double time, double shift) {
    super(market);
    Validate.notNull(index, "Index");
    _index = index;
    _time = time;
    _shift = shift;
    _forwardCurve = getCurve(index);
  }

  @Override
  public double getForwardRate(IborIndex index, double startTime, double endTime, double accuralFactor) {
    if (index == _index) {
      if (_time == startTime) {
        double rateStart = -Math.log(_forwardCurve.getDiscountFactor(_time)) / _time;
        double dfStart = Math.exp(-(rateStart + _shift) * _time);
        return (dfStart / _forwardCurve.getDiscountFactor(endTime) - 1) / accuralFactor;
      }
      if (_time == endTime) {
        double rateEnd = -Math.log(_forwardCurve.getDiscountFactor(_time)) / _time;
        double dfEnd = Math.exp(-(rateEnd + _shift) * _time);
        return (_forwardCurve.getDiscountFactor(startTime) / dfEnd - 1) / accuralFactor;
      }
    }
    return super.getForwardRate(index, startTime, endTime, accuralFactor);
  }

}