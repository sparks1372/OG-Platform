/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.util.tuple.ObjectsPair;

/**
 * Fudge builder for {@code ObjectsPair}.
 */
@GenericFudgeBuilderFor(ObjectsPair.class)
public final class ObjectsPairBuilder implements FudgeBuilder<ObjectsPair<?, ?>> {

  /** Field name. */
  public static final String FIRST_FIELD_NAME = "first";
  /** Field name. */
  public static final String SECOND_FIELD_NAME = "second";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ObjectsPair<?, ?> object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    if (object.getFirst() != null) {
      serializer.addToMessageWithClassHeaders(msg, FIRST_FIELD_NAME, null, object.getFirst());
    }
    if (object.getSecond() != null) {
      serializer.addToMessageWithClassHeaders(msg, SECOND_FIELD_NAME, null, object.getSecond());
    }
    return msg;
  }

  @Override
  public ObjectsPair<?, ?> buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Object first;
    if (msg.hasField(FIRST_FIELD_NAME)) {
      first = deserializer.fieldValueToObject(msg.getByName(FIRST_FIELD_NAME));
    } else {
      first = null;
    }
    Object second;
    if (msg.hasField(SECOND_FIELD_NAME)) {
      second = deserializer.fieldValueToObject(msg.getByName(SECOND_FIELD_NAME));
    } else {
      second = null;
    }
    return ObjectsPair.of(first, second);
  }
  

}
