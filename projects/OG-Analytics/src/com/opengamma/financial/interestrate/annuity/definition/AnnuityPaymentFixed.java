/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * A wrapper class for a GenericAnnuity containing PaymentFixed. Useful for amortized bond nominal repayments.
 */
public class AnnuityPaymentFixed extends GenericAnnuity<PaymentFixed> {

  /**
   * Constructor from an array of fixed payments.
   * @param payments The payments array.
   */
  public AnnuityPaymentFixed(final PaymentFixed[] payments) {
    super(payments);
  }

  /**
   * Remove the payments paying on or before the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  @Override
  public AnnuityPaymentFixed trimBefore(double trimTime) {
    List<PaymentFixed> list = new ArrayList<PaymentFixed>();
    for (PaymentFixed payment : getPayments()) {
      if (payment.getPaymentTime() > trimTime) {
        list.add(payment);
      }
    }
    return new AnnuityPaymentFixed(list.toArray(getPayments()));
  }

}
