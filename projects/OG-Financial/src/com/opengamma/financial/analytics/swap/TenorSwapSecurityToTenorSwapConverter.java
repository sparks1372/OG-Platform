/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.swap;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 */
public class TenorSwapSecurityToTenorSwapConverter {

  //private static final Logger s_logger = LoggerFactory.getLogger(TenorSwapSecurityToTenorSwapConverter.class);
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final ConventionBundleSource _conventionSource;

  public TenorSwapSecurityToTenorSwapConverter(final HolidaySource holidaySource, final RegionSource regionSource, final ConventionBundleSource conventionSource) {
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _conventionSource = conventionSource;

  }

  // REVIEW: jim 8-Oct-2010 -- we might want to move this logic inside the RegionMaster.
  protected Calendar getCalendar(final ExternalId regionId) {
    if (regionId.isScheme(RegionUtils.FINANCIAL) && regionId.getValue().contains("+")) {
      final String[] regions = regionId.getValue().split("\\+");
      final Set<Region> resultRegions = new HashSet<Region>();
      for (final String region : regions) {
        resultRegions.add(_regionSource.getHighestLevelRegion(RegionUtils.financialRegionId(region)));
      }
      return new HolidaySourceCalendarAdapter(_holidaySource, resultRegions);
    } else {
      final Region payRegion = _regionSource.getHighestLevelRegion(regionId); // we've checked that they are the same.
      return new HolidaySourceCalendarAdapter(_holidaySource, payRegion);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public TenorSwap<?> getSwap(final SwapSecurity swapSecurity, final String fundingCurveName, final String payLegCurveName, final String recieveLegCurveName, final double marketRate,
      final ZonedDateTime now) {

    Validate.notNull(swapSecurity, "swap security");
    final ZonedDateTime effectiveDate = swapSecurity.getEffectiveDate();
    final ZonedDateTime maturityDate = swapSecurity.getMaturityDate();

    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();

    FloatingInterestRateLeg floatPayLeg;
    FloatingInterestRateLeg floatReceiveLeg;
    if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      floatPayLeg = (FloatingInterestRateLeg) payLeg;
      floatReceiveLeg = (FloatingInterestRateLeg) receiveLeg;
    } else {
      throw new OpenGammaRuntimeException("can only handle float-float legs");
    }

    if (!payLeg.getRegionIdentifier().equals(receiveLeg.getRegionIdentifier())) {
      throw new OpenGammaRuntimeException("Pay and receive legs must be from same region");
    }

    final Calendar calendar = getCalendar(payLeg.getRegionIdentifier());

    final String currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency().getCode();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_TENOR_SWAP"));

    final AnnuityCouponIbor pay = getFloatLeg(floatPayLeg, now, effectiveDate, maturityDate, fundingCurveName, payLegCurveName, calendar, 0.0 /*spread is paid on receive leg*/,
        conventions.getBasisSwapPayFloatingLegSettlementDays(), true);

    final AnnuityCouponIbor receive = getFloatLeg(floatReceiveLeg, now, effectiveDate, maturityDate, fundingCurveName, recieveLegCurveName, calendar, marketRate,
        conventions.getBasisSwapReceiveFloatingLegSettlementDays(), false);

    // TODO: add payer/receiver flag!

    return new TenorSwap(pay, receive);
  }

  public AnnuityCouponIbor getFloatLeg(final FloatingInterestRateLeg floatLeg, final ZonedDateTime now, final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate,
      final String fundingCurveName, final String liborCurveName, final Calendar calendar, final double marketRate, final int settlementDays, boolean isPayer) {
    final ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, floatLeg.getFrequency());
    final ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, 0);
    final ZonedDateTime[] resetDates = ScheduleCalculator.getAdjustedResetDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, settlementDays);
    final ZonedDateTime[] maturityDates = ScheduleCalculator.getAdjustedMaturityDateSchedule(effectiveDate, unadjustedDates, floatLeg.getBusinessDayConvention(), calendar, floatLeg.getFrequency());

    final double[] paymentTimes = ScheduleCalculator.getTimes(adjustedDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), now);
    final double[] resetTimes = ScheduleCalculator.getTimes(resetDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), now);
    final double[] maturityTimes = ScheduleCalculator.getTimes(maturityDates, DayCountFactory.INSTANCE.getDayCount("Actual/Actual"), now);
    final double[] yearFractions = ScheduleCalculator.getYearFractions(adjustedDates, floatLeg.getDayCount(), effectiveDate);
    // Implementation comment: negative notional if payer.
    final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount() * (isPayer ? -1.0 : 1.0);

    final double[] spreads = new double[paymentTimes.length];
    Arrays.fill(spreads, marketRate);

    final CouponIbor[] payments = new CouponIbor[paymentTimes.length];
    for (int i = 0; i < payments.length; i++) {
      payments[i] = new CouponIbor(((InterestRateNotional) floatLeg.getNotional()).getCurrency(), paymentTimes[i], fundingCurveName, yearFractions[i], notional, resetTimes[i], resetTimes[i],
          maturityTimes[i], yearFractions[i], spreads[i], liborCurveName);
    }

    //TODO need to handle paymentYearFraction differently from forwardYearFraction 
    return new AnnuityCouponIbor(payments);
  }

}
