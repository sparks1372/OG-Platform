/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * Class with methods related to bond transaction valued by discounting.
 */
public final class BondTransactionDiscountingMethod {

  /**
   * The unique instance of the class.
   */
  private static final BondTransactionDiscountingMethod INSTANCE = new BondTransactionDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private BondTransactionDiscountingMethod() {
  }

  /**
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  /**
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueSensitivityCalculator PVSC = PresentValueSensitivityCalculator.getInstance();

  /**
   * Compute the present value of a bond transaction.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public double presentValue(final BondTransaction<? extends BondSecurity<? extends Payment>> bond, final YieldCurveBundle curves) {
    final double pvNominal = PVC.visit(bond.getBondTransaction().getNominal(), curves);
    final double pvCoupon = PVC.visit(bond.getBondTransaction().getCoupon(), curves);
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), bond.getSettlementAmount(), bond.getBondTransaction()
        .getRepoCurveName());
    final double pvSettlement = PVC.visit(settlement, curves);
    return (pvNominal + pvCoupon) * bond.getQuantity() + pvSettlement;
  }

  /**
   * Compute the present value of a bond transaction from its clean price.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @param cleanPrice The bond clean price.
   * @return The present value.
   */
  public double presentValueFromCleanPrice(final BondTransaction<? extends BondSecurity<? extends Payment>> bond, final YieldCurveBundle curves, final double cleanPrice) {
    Validate.isTrue(bond instanceof BondFixedTransaction, "Present value from clean price only for fixed coupon bond");
    final BondFixedTransaction bondFixed = (BondFixedTransaction) bond;
    final double dfSettle = curves.getCurve(bondFixed.getBondStandard().getRepoCurveName()).getDiscountFactor(bondFixed.getBondTransaction().getSettlementTime());
    final double pvPriceStandard = (cleanPrice * bondFixed.getNotionalStandard() + bondFixed.getBondStandard().getAccruedInterest()) * dfSettle;
    final double pvNominalStandard = PVC.visit(bond.getBondStandard().getNominal(), curves);
    final double pvCouponStandard = PVC.visit(bond.getBondStandard().getCoupon(), curves);
    final double pvDiscountingStandard = (pvNominalStandard + pvCouponStandard);
    final double pvNominalTransaction = PVC.visit(bond.getBondTransaction().getNominal(), curves);
    final double pvCouponTransaction = PVC.visit(bond.getBondTransaction().getCoupon(), curves);
    final double pvDiscountingTransaction = (pvNominalTransaction + pvCouponTransaction);
    return (pvDiscountingTransaction - pvDiscountingStandard) * bond.getQuantity() + pvPriceStandard;
  }

  /**
   * Compute the present value sensitivity of a bond transaction.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @return The present value sensitivity.
   */
  public PresentValueSensitivity presentValueSensitivity(final BondTransaction<? extends BondSecurity<? extends Payment>> bond, final YieldCurveBundle curves) {
    final PresentValueSensitivity pvsNominal = new PresentValueSensitivity(PVSC.visit(bond.getBondTransaction().getNominal(), curves));
    final PresentValueSensitivity pvsCoupon = new PresentValueSensitivity(PVSC.visit(bond.getBondTransaction().getCoupon(), curves));
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), bond.getSettlementAmount(), bond.getBondTransaction()
        .getRepoCurveName());
    final PresentValueSensitivity pvsSettlement = new PresentValueSensitivity(PVSC.visit(settlement, curves));
    return pvsNominal.add(pvsCoupon).multiply(bond.getQuantity()).add(pvsSettlement);
  }
}
