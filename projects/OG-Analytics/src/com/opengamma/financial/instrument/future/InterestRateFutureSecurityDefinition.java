/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.schedule.ScheduleCalculator;

/**
 * Description of an interest rate future security.
 */
public class InterestRateFutureSecurityDefinition {

  /**
   * Future last trading date. Usually the date for which the third Wednesday of the month is the spot date.
   */
  private final ZonedDateTime _lastTradingDate;
  /**
   * Ibor index associated to the future.
   */
  private final IborIndex _iborIndex;
  /**
   * Fixing period of the reference Ibor starting date.
   */
  private final ZonedDateTime _fixingPeriodStartDate;
  /**
   * Fixing period of the reference Ibor end date.
   */
  private final ZonedDateTime _fixingPeriodEndDate;
  /**
   * Fixing period of the reference Ibor accrual factor.
   */
  private final double _fixingPeriodAccrualFactor;
  /**
   * Future notional.
   */
  private final double _notional;
  /**
   * Future payment accrual factor. Usually a standardized number of 0.25 for a 3M future.
   */
  private final double _paymentAccrualFactor;
  /**
   * Future name.
   */
  private final String _name;

  /**
   * Constructor of the interest rate future security.
   * @param lastTradingDate Future last trading date.
   * @param iborIndex Ibor index associated to the future.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor. 
   * @param name Future name.
   */
  public InterestRateFutureSecurityDefinition(final ZonedDateTime lastTradingDate, final IborIndex iborIndex, final double notional, final double paymentAccrualFactor, final String name) {
    Validate.notNull(lastTradingDate, "Last trading date");
    Validate.notNull(iborIndex, "Ibor index");
    Validate.notNull(name, "Name");
    this._lastTradingDate = lastTradingDate;
    this._iborIndex = iborIndex;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(_lastTradingDate, _iborIndex.getBusinessDayConvention(), _iborIndex.getCalendar(), _iborIndex.getSettlementDays());
    _fixingPeriodEndDate = ScheduleCalculator
        .getAdjustedDate(_fixingPeriodStartDate, _iborIndex.getBusinessDayConvention(), _iborIndex.getCalendar(), _iborIndex.isEndOfMonth(), _iborIndex.getTenor());
    _fixingPeriodAccrualFactor = _iborIndex.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate);
    this._notional = notional;
    this._paymentAccrualFactor = paymentAccrualFactor;
    _name = name;
  }

  /**
   * Constructor of the interest rate future security.
   * @param lastTradingDate Future last trading date.
   * @param iborIndex Ibor index associated to the future.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor.
   */
  public InterestRateFutureSecurityDefinition(final ZonedDateTime lastTradingDate, final IborIndex iborIndex, final double notional, final double paymentAccrualFactor) {
    this(lastTradingDate, iborIndex, notional, paymentAccrualFactor, "RateFuture " + iborIndex.getName());
  }

  /**
   * Gets the future last trading date.
   * @return The last trading date.
   */
  public ZonedDateTime getLastTradingDate() {
    return _lastTradingDate;
  }

  /**
   * Gets the Ibor index associated to the future.
   * @return The Ibor index
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  /**
   * Gets the fixing period of the reference Ibor starting date.
   * @return The fixing period starting date
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the fixing period of the reference Ibor end date.
   * @return The fixing period end date.
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the fixing period of the reference Ibor accrual factor.
   * @return The Fixing period accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the future notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the future payment accrual factor.
   * @return The future payment accrual factor.
   */
  public double getPaymentAccrualFactor() {
    return _paymentAccrualFactor;
  }

  /**
   * Gets the future name.
   * @return The name
   */
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fixingPeriodEndDate.hashCode();
    result = prime * result + _fixingPeriodStartDate.hashCode();
    result = prime * result + _iborIndex.hashCode();
    result = prime * result + _lastTradingDate.hashCode();
    result = prime * result + _name.hashCode();
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    InterestRateFutureSecurityDefinition other = (InterestRateFutureSecurityDefinition) obj;
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodEndDate, other._fixingPeriodEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodStartDate, other._fixingPeriodStartDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    if (!ObjectUtils.equals(_lastTradingDate, other._lastTradingDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentAccrualFactor) != Double.doubleToLongBits(other._paymentAccrualFactor)) {
      return false;
    }
    return true;
  }

}