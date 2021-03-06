/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearExtrapolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Holds Fudge builders for the interpolation model.
 */
/* package */final class MathInterpolation {

  /**
   * Restricted constructor.
   */
  private MathInterpolation() {
  }

  //-------------------------------------------------------------------------
  /**
   * Fudge builder for {@code Interpolator1D}.
   */
  @GenericFudgeBuilderFor(Interpolator1D.class)
  public static final class Interpolator1DBuilder implements FudgeBuilder<Interpolator1D<? extends Interpolator1DDataBundle>> {
    private static final String TYPE_FIELD_NAME = "type";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Interpolator1D<? extends Interpolator1DDataBundle> object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(0, Interpolator1D.class.getName());
      message.add(TYPE_FIELD_NAME, Interpolator1DFactory.getInterpolatorName(object));
      return message;
    }

    @Override
    public Interpolator1D<? extends Interpolator1DDataBundle> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      return Interpolator1DFactory.getInterpolator(message.getFieldValue(String.class, message.getByName(TYPE_FIELD_NAME)));
    }
  }

  /**
   * Fudge builder for {@code CombinedInterpolatorExtrapolator}.
   */
  @FudgeBuilderFor(CombinedInterpolatorExtrapolator.class)
  public static final class CombinedInterpolatorExtrapolatorBuilder extends AbstractFudgeBuilder<CombinedInterpolatorExtrapolator<?>> {
    private static final String LEFT_EXTRAPOLATOR_FIELD_NAME = "leftExtrapolator";
    private static final String RIGHT_EXTRAPOLATOR_FIELD_NAME = "rightExtrapolator";
    private static final String INTERPOLATOR_FIELD_NAME = "interpolator";

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final CombinedInterpolatorExtrapolator<?> object) {
      final Interpolator1D<?> interpolator = object.getInterpolator();
      message.add(INTERPOLATOR_FIELD_NAME, Interpolator1DFactory.getInterpolatorName(interpolator));
      message.add(LEFT_EXTRAPOLATOR_FIELD_NAME, Interpolator1DFactory.getInterpolatorName(object.getLeftExtrapolator()));
      message.add(RIGHT_EXTRAPOLATOR_FIELD_NAME, Interpolator1DFactory.getInterpolatorName(object.getRightExtrapolator()));
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String interpolatorName = message.getString(INTERPOLATOR_FIELD_NAME);
      final String leftExtrapolatorName = message.getString(LEFT_EXTRAPOLATOR_FIELD_NAME);
      final String rightExtrapolatorName = message.getString(RIGHT_EXTRAPOLATOR_FIELD_NAME);
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
      final Interpolator1D<? extends Interpolator1DDataBundle> leftExtrapolator = getExtrapolator(leftExtrapolatorName, interpolator);
      final Interpolator1D<? extends Interpolator1DDataBundle> rightExtrapolator = getExtrapolator(rightExtrapolatorName, interpolator);
      return new CombinedInterpolatorExtrapolator(interpolator, leftExtrapolator, rightExtrapolator);
    }

    private <T extends Interpolator1DDataBundle> Interpolator1D<T> getExtrapolator(final String extrapolatorName, final Interpolator1D<T> interpolator) {
      if (extrapolatorName.equals(Interpolator1DFactory.FLAT_EXTRAPOLATOR)) {
        return new FlatExtrapolator1D<T>();
      }
      if (extrapolatorName.equals(Interpolator1DFactory.LINEAR_EXTRAPOLATOR)) {
        return new LinearExtrapolator1D<T>(interpolator);
      }
      return null;
    }
  }

  /**
   * Fudge builder for {@code GridInterpolator2D}.
   */
  @GenericFudgeBuilderFor(GridInterpolator2D.class)
  public static final class GridInterpolator2DBuilder extends AbstractFudgeBuilder<GridInterpolator2D<? extends Interpolator1DDataBundle, ? extends Interpolator1DDataBundle>> {
    private static final String X_FIELD_NAME = "x";
    private static final String Y_FIELD_NAME = "y";

    @Override
    public void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message,
        final GridInterpolator2D<? extends Interpolator1DDataBundle, ? extends Interpolator1DDataBundle> object) {
      serializer.addToMessage(message, X_FIELD_NAME, null, object.getXInterpolator());
      serializer.addToMessage(message, Y_FIELD_NAME, null, object.getYInterpolator());
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public GridInterpolator2D<? extends Interpolator1DDataBundle, ? extends Interpolator1DDataBundle> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      return new GridInterpolator2D(deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(X_FIELD_NAME)),
          deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(Y_FIELD_NAME)));
    }
  }
}
