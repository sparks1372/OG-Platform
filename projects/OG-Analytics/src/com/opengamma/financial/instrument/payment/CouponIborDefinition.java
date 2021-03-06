/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a Ibor-like floating coupon.
 */
public class CouponIborDefinition extends CouponFloatingDefinition {

  /**
   * Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IborIndex _index;
  /**
   * The start date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodStartDate;
  /**
   * The end date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodEndDate;
  /**
   * The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   */
  private final double _fixingPeriodAccrualFactor;

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index. The payment currency is the index currency.
   * 
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   */
  public CouponIborDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final IborIndex index) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    Validate.notNull(index, "index");
    Validate.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _index = index;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getBusinessDayConvention(), _index.getCalendar(), _index.getSettlementDays());
    _fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate, index.getBusinessDayConvention(), index.getCalendar(), index.isEndOfMonth(), index.getTenor());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate);
  }

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index. The payment currency is the index currency.
   * 
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @return The Ibor coupon.
   */
  public static CouponIborDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor, final double notional,
      final ZonedDateTime fixingDate, final IborIndex index) {
    Validate.notNull(index, "index");
    return new CouponIborDefinition(index.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index);
  }

  /**
   * Builder of Ibor-like coupon from the fixing date and the index. The payment and accrual dates are the one of the fixing period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @return The Ibor coupon.
   */
  public static CouponIborDefinition from(final double notional, final ZonedDateTime fixingDate, final IborIndex index) {
    Validate.notNull(fixingDate, "fixing date");
    Validate.notNull(index, "index");
    final ZonedDateTime fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, index.getBusinessDayConvention(), index.getCalendar(), index.getSettlementDays());
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, index.getBusinessDayConvention(), index.getCalendar(), index.isEndOfMonth(), index.getTenor());
    final double fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate);
    return new CouponIborDefinition(index.getCurrency(), fixingPeriodEndDate, fixingPeriodStartDate, fixingPeriodEndDate, fixingPeriodAccrualFactor, notional, fixingDate, index);
  }

  /**
   * Builder of Ibor-like coupon from an underlying coupon, the fixing date and the index. The fixing period dates are deduced from the index and the fixing date.
   * @param coupon Underlying coupon.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @return The Ibor coupon.
   */
  public static CouponIborDefinition from(final CouponDefinition coupon, final ZonedDateTime fixingDate, final IborIndex index) {
    Validate.notNull(coupon, "coupon");
    Validate.notNull(fixingDate, "fixing date");
    Validate.notNull(index, "index");
    return new CouponIborDefinition(index.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        fixingDate, index);
  }

  /**
   * Builder from an Ibor coupon with spread. The spread is ignored.
   * @param coupon The coupon with spread.
   * @return The ibor coupon.
   */
  public static CouponIborDefinition from(final CouponIborSpreadDefinition coupon) {
    Validate.notNull(coupon, "coupon");
    return new CouponIborDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(),
        coupon.getFixingDate(), coupon.getIndex());
  }

  /**
   * Gets the Ibor index of the instrument.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the fixing period start date.
   * @return The fixing period start date.
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the fixindPeriodEndDate field.
   * @return the fixindPeriodEndDate
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the fixingPeriodAccrualFactor field.
   * @return the fixingPeriodAccrualFactor
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  @Override
  public String toString() {
    return super.toString() + " *Ibor coupon* Index = " + _index + ", Fixing period = [" + _fixingPeriodStartDate + " - " + _fixingPeriodEndDate + " - " + _fixingPeriodAccrualFactor + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _fixingPeriodEndDate.hashCode();
    result = prime * result + _fixingPeriodStartDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CouponIborDefinition other = (CouponIborDefinition) obj;
    if (!ObjectUtils.equals(_fixingPeriodEndDate, other._fixingPeriodEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodStartDate, other._fixingPeriodStartDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitCouponIbor(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCouponIbor(this);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " " + date);
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double paymentTime = actAct.getDayCountFraction(date, getPaymentDate());
    final double fixingTime = actAct.getDayCountFraction(date, getFixingDate());
    final double fixingPeriodStartTime = actAct.getDayCountFraction(date, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(date, getFixingPeriodEndDate());
    //TODO: Definition has no spread and time version has one: to be standardized.
    return new CouponIbor(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), forwardCurveName);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(indexFixingTimeSeries, "Index fixing time series");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String fundingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double paymentTime = actAct.getDayCountFraction(date, getPaymentDate());

    if (date.isAfter(getFixingDate()) || (date.equals(getFixingDate()))) {
      Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
      //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
      if (fixedRate == null) {
        final ZonedDateTime fixingDateAtLiborFixingTime = getFixingDate().withTime(11, 0);
        fixedRate = indexFixingTimeSeries.getValue(fixingDateAtLiborFixingTime);
      }
      if (fixedRate == null) {
        final ZonedDateTime previousBusinessDay = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding").adjustDate(getIndex().getConvention().getWorkingDayCalendar(),
            getFixingDate().minusDays(1));
        fixedRate = indexFixingTimeSeries.getValue(previousBusinessDay);
        //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
        if (fixedRate == null) {
          final ZonedDateTime previousBusinessDayAtLiborFixingTime = previousBusinessDay.withTime(11, 0);
          fixedRate = indexFixingTimeSeries.getValue(previousBusinessDayAtLiborFixingTime);
        }
        if (fixedRate == null) {
          fixedRate = indexFixingTimeSeries.getLatestValue(); //TODO remove me as soon as possible
          //throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDate());
        }
      }
      //      //TODO this is a fix so that a single payment swap is still sensitive to the forward curve even though the payment is fixed (i.e. the reset date has passed)
      final double fixingTime = 0.0;
      double fixingPeriodStartTime = 0.0; //TODO How should this be handled?
      if (date.isBefore(getFixingPeriodStartDate())) {
        fixingPeriodStartTime = actAct.getDayCountFraction(date, getFixingPeriodStartDate());
      }
      final double fixingPeriodEndTime = actAct.getDayCountFraction(date, getFixingPeriodEndDate());
      return new CouponIborFixed(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixedRate, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
          getFixingPeriodAccrualFactor(), 0.0, forwardCurveName);
    }

    final double fixingTime = actAct.getDayCountFraction(date, getFixingDate());
    final double fixingPeriodStartTime = actAct.getDayCountFraction(date, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(date, getFixingPeriodEndDate());
    //TODO: Definition has no spread and time version has one: to be standardized.
    return new CouponIbor(getCurrency(), paymentTime, fundingCurveName, getPaymentYearFraction(), getNotional(), fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), forwardCurveName);
  }
}
