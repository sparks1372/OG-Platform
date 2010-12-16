/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * Test OneOneDatCount.
 */
public class OneOneDayCountTest {

  private static final ZonedDateTime D1 = DateUtil.getUTCDate(2010, 1, 1);
  private static final ZonedDateTime D2 = DateUtil.getUTCDate(2011, 1, 1);
  private static final OneOneDayCount DC = new OneOneDayCount();

  @Test
  public void test() {
    assertEquals(DC.getDayCountFraction(D1, D2), 1, 0);
    final double coupon = 0.04;
    final int paymentsPerYear = 4;
    assertEquals(DC.getAccruedInterest(D1, D2, D2, coupon, paymentsPerYear), coupon / paymentsPerYear, 0);
    assertEquals(DC.getConventionName(), "1/1");
  }

}