/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.inflation;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.FixedIncomeInstrumentWithDataConverter;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing an zero-coupon inflation coupon were the inflation figure are the one of the reference month (not interpolated) and the inflation increment is multiplied by a gearing factor.
 * The start index value is known when the coupon is traded/issued.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is factor*(Index_End / Index_Start - X) with X=0 for notional payment and X=1 for no notional payment.
 */
public class CouponInflationZeroCouponMonthlyGearingDefinition extends CouponDefinition implements FixedIncomeInstrumentWithDataConverter<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The price index associated to the coupon.
   */
  private final PriceIndex _priceIndex;
  /**
   * The reference date for the index at the coupon start. May not be relevant as the index value is known.
   */
  private final ZonedDateTime _referenceStartDate;
  /**
   * The index value at the start of the coupon.
   */
  private final double _indexStartValue;
  /**
   * The reference date for the index at the coupon end. The first of the month. There is usually a difference of two or three month between the reference date and the payment date.
   */
  private final ZonedDateTime _referenceEndDate;
  /**
   * The date on which the end index is expected to be known. The index is usually known two week after the end of the reference month. 
   * The date is only an "expected date" as the index publication could be delayed for different reasons. The date should not be enforced to strictly in pricing and instrument creation.
   */
  private final ZonedDateTime _fixingEndDate;
  /**
   * Flag indicating if the notional is paid (true) or not (false).
   */
  private final boolean _payNotional;
  /**
   * The gearing (multiplicative) factor applied to the inflation increment rate.
   */
  private final double _factor;

  /**
   * Constructor for zero-coupon inflation coupon.
   * @param currency The coupon currency.
   * @param paymentDate The payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon. 
   * @param referenceStartDate The reference date for the index at the coupon start.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param fixingEndDate The date on which the end index is expected to be known.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param factor The multiplicative factor.
   */
  public CouponInflationZeroCouponMonthlyGearingDefinition(Currency currency, ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double paymentYearFraction,
      double notional, PriceIndex priceIndex, ZonedDateTime referenceStartDate, double indexStartValue, ZonedDateTime referenceEndDate, ZonedDateTime fixingEndDate, boolean payNotional, 
      double factor) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    Validate.notNull(priceIndex, "Price index");
    Validate.notNull(referenceStartDate, "Reference start date");
    Validate.notNull(referenceEndDate, "Reference end date");
    Validate.notNull(fixingEndDate, "Fixing end date");
    this._priceIndex = priceIndex;
    this._referenceStartDate = referenceStartDate;
    this._indexStartValue = indexStartValue;
    this._referenceEndDate = referenceEndDate;
    this._fixingEndDate = fixingEndDate;
    _payNotional = payNotional;
    _factor = factor;
  }

  /**
   * Builder for inflation zero-coupon. 
   * The accrualStartDate is used for the referenceStartDate. The paymentDate is used for accrualEndDate. The paymentYearFraction is 1.0. The notional is not paid in the coupon.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon. 
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param fixingEndDate The date on which the end index is expected to be known.
   * @param factor The multiplicative factor.
   * @return The coupon.
   */
  public static CouponInflationZeroCouponMonthlyGearingDefinition from(ZonedDateTime accrualStartDate, ZonedDateTime paymentDate, double notional, PriceIndex priceIndex, double indexStartValue,
      ZonedDateTime referenceEndDate, ZonedDateTime fixingEndDate, double factor) {
    Validate.notNull(priceIndex, "Price index");
    return new CouponInflationZeroCouponMonthlyGearingDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex, accrualStartDate, indexStartValue,
        referenceEndDate, fixingEndDate, false, factor);
  }

  /**
   * Builder for inflation zero-coupon based on an inflation lag and index publication. The fixing date is the publication lag after the last reference month.
   * The end accrual date is the payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon. 
   * @param indexStartValue The index value at the start of the coupon.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param factor The multiplicative factor.
   * @return The inflation zero-coupon.
   */
  public static CouponInflationZeroCouponMonthlyGearingDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional, final PriceIndex priceIndex,
      final double indexStartValue, final int monthLag, boolean payNotional, double factor) {
    ZonedDateTime referenceStartDate = accrualStartDate.minusMonths(monthLag);
    ZonedDateTime referenceEndDate = paymentDate.minusMonths(monthLag);
    referenceStartDate = referenceStartDate.withDayOfMonth(1);
    referenceEndDate = referenceEndDate.withDayOfMonth(1);
    ZonedDateTime fixingDate = referenceEndDate.minusDays(1).withDayOfMonth(1).plusMonths(2).plus(priceIndex.getPublicationLag());
    return new CouponInflationZeroCouponMonthlyGearingDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex, referenceStartDate, indexStartValue,
        referenceEndDate, fixingDate, payNotional, factor);
  }

  /**
   * Builder for inflation zero-coupon based on an inflation lag and index publication. The fixing date is the publication lag after the last reference month.
   * @param paymentDate The payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon. 
   * @param indexStartValue The index value at the start of the coupon.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param factor The multiplicative factor.
   * @return The inflation zero-coupon.
   */
  public static CouponInflationZeroCouponMonthlyGearingDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double notional, final PriceIndex priceIndex, final double indexStartValue, final int monthLag, boolean payNotional, double factor) {
    ZonedDateTime referenceStartDate = accrualStartDate.minusMonths(monthLag);
    ZonedDateTime referenceEndDate = paymentDate.minusMonths(monthLag);
    referenceStartDate = referenceStartDate.withDayOfMonth(1);
    referenceEndDate = referenceEndDate.withDayOfMonth(1);
    ZonedDateTime fixingDate = referenceEndDate.minusDays(1).withDayOfMonth(1).plusMonths(2).plus(priceIndex.getPublicationLag());
    return new CouponInflationZeroCouponMonthlyGearingDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, 1.0, notional, priceIndex, referenceStartDate,
        indexStartValue, referenceEndDate, fixingDate, payNotional, factor);
  }

  /**
   * Gets the price index associated to the coupon. 
   * @return The price index.
   */
  public PriceIndex getPriceIndex() {
    return _priceIndex;
  }

  /**
   * Gets the reference date for the index at the coupon start.
   * @return The reference date for the index at the coupon start.
   */
  public ZonedDateTime getReferenceStartDate() {
    return _referenceStartDate;
  }

  /**
   * Gets the index value at the start of the coupon.
   * @return The index value at the start of the coupon.
   */
  public double getIndexStartValue() {
    return _indexStartValue;
  }

  /**
   * Gets the reference date for the index at the coupon end.
   * @return The reference date for the index at the coupon end.
   */
  public ZonedDateTime getReferenceEndDate() {
    return _referenceEndDate;
  }

  /**
   * Gets the date on which the end index is expected to be known.
   * @return The date on which the end index is expected to be known.
   */
  public ZonedDateTime getFixingEndDate() {
    return _fixingEndDate;
  }

  /**
   * Gets the pay notional flag.
   * @return The flag.
   */
  public boolean payNotional() {
    return _payNotional;
  }

  /**
   * Gets the multiplicative factor.
   * @return The factor.
   */
  public double getFactor() {
    return _factor;
  }

  @Override
  public CouponInflationZeroCouponMonthlyGearing toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "Do not have any fixing data but are asking for a derivative after the payment date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    double referenceEndTime = TimeCalculator.getTimeBetween(date, getReferenceEndDate());
    double fixingTime = TimeCalculator.getTimeBetween(date, getFixingEndDate());
    final String discountingCurveName = yieldCurveNames[0];
    return new CouponInflationZeroCouponMonthlyGearing(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), _priceIndex, _indexStartValue, referenceEndTime,
        fixingTime, _payNotional, _factor);
  }

  @Override
  public Coupon toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    //    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final String discountingCurveName = yieldCurveNames[0];
    boolean fixingKnown = false;
    double rate = 0.0;
    if (!_fixingEndDate.isAfter(date)) { // Fixing data to be checked
      ZonedDateTime requiredIndexDate = _referenceEndDate;
      Double knownIndex = priceIndexTimeSeries.getValue(requiredIndexDate);
      if (knownIndex != null) { // Fixing known
        fixingKnown = true;
        rate = _factor * (knownIndex / _indexStartValue - (_payNotional ? 0.0 : 1.0));
      }
    }
    if (fixingKnown) {
      return new CouponFixed(getCurrency(), paymentTime, discountingCurveName, 1.0, getNotional(), rate);
    }
    double fixingTime = 0; // The reference index is expected to be known but is not known yet.
    if (_fixingEndDate.isAfter(date)) {
      fixingTime = TimeCalculator.getTimeBetween(date, _fixingEndDate);
    }
    double referenceEndTime = 0.0;
    referenceEndTime = TimeCalculator.getTimeBetween(date, _referenceEndDate);
    return new CouponInflationZeroCouponMonthlyGearing(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), _priceIndex, _indexStartValue, referenceEndTime,
        fixingTime, _payNotional, _factor);
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitCouponInflationZeroCouponMonthlyGearing(this, data);
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCouponInflationZeroCouponMonthlyGearing(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_factor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fixingEndDate.hashCode();
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_payNotional ? 1231 : 1237);
    result = prime * result + _priceIndex.hashCode();
    result = prime * result + _referenceEndDate.hashCode();
    result = prime * result + _referenceStartDate.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CouponInflationZeroCouponMonthlyGearingDefinition other = (CouponInflationZeroCouponMonthlyGearingDefinition) obj;
    if (Double.doubleToLongBits(_factor) != Double.doubleToLongBits(other._factor)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingEndDate, other._fixingEndDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (_payNotional != other._payNotional) {
      return false;
    }
    if (!ObjectUtils.equals(_priceIndex, other._priceIndex)) {
      return false;
    }
    if (!ObjectUtils.equals(_referenceEndDate, other._referenceEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_referenceStartDate, other._referenceStartDate)) {
      return false;
    }
    return true;
  }

}