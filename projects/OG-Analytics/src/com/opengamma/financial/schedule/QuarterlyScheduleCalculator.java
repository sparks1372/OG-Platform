/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.schedule;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class QuarterlyScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      return new LocalDate[] {startDate};
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    if (fromEnd) {
      LocalDate date = endDate;
      int i = 3;
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = generateRecursive ? date.minus(Period.ofMonths(3)) : endDate.minus(Period.ofMonths(i));
        i += 3;
      }
      return getReversedDates(dates);
    }
    LocalDate date = startDate;
    int i = 3;
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = generateRecursive ? date.plus(Period.ofMonths(3)) : startDate.plus(Period.ofMonths(i));
      i += 3;
    }
    return dates.toArray(EMPTY_LOCAL_DATE_ARRAY);
  }

  @Override
  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd, final boolean generateRecursive) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      return new ZonedDateTime[] {startDate};
    }
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    if (fromEnd) {
      ZonedDateTime date = endDate;
      int i = 3;
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = generateRecursive ? date.minus(Period.ofMonths(3)) : endDate.minus(Period.ofMonths(i));
        i += 3;
      }
      return getReversedDates(dates);
    }
    ZonedDateTime date = startDate;
    int i = 3;
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = generateRecursive ? date.plus(Period.ofMonths(3)) : startDate.plus(Period.ofMonths(i));
      i += 3;
    }
    return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }

}
