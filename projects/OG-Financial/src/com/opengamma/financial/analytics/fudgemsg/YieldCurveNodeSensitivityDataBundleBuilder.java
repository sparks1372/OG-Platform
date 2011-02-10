/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;

/**
 * 
 */
@FudgeBuilderFor(YieldCurveNodeSensitivityDataBundle.class)
public class YieldCurveNodeSensitivityDataBundleBuilder extends FudgeBuilderBase<YieldCurveNodeSensitivityDataBundle> {
  private static final String MATRIX_NAME = "Matrix";
  private static final String CURRENCY_NAME = "Currency";
  private static final String CURVE_NAME = "CurveName";

  @Override
  public YieldCurveNodeSensitivityDataBundle buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final Currency ccy = context.fieldValueToObject(Currency.class, message.getByName(CURRENCY_NAME));
    final DoubleLabelledMatrix1D labelledMatrix = context.fieldValueToObject(DoubleLabelledMatrix1D.class, message.getByName(MATRIX_NAME));
    final String curveName = context.fieldValueToObject(String.class, message.getByName(CURVE_NAME));
    return new YieldCurveNodeSensitivityDataBundle(ccy, labelledMatrix, curveName);
  }

  @Override
  protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final YieldCurveNodeSensitivityDataBundle object) {
    context.objectToFudgeMsg(message, CURRENCY_NAME, null, object.getCurrency());
    context.objectToFudgeMsg(message, MATRIX_NAME, null, object.getLabelledMatrix());
    context.objectToFudgeMsg(message, CURVE_NAME, null, object.getYieldCurveName());
  }

}