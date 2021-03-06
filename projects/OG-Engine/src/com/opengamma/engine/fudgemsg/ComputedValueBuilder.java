/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Fudge message builder for {@code ComputedValue}.
 */
@FudgeBuilderFor(ComputedValue.class)
public class ComputedValueBuilder implements FudgeBuilder<ComputedValue> {
  /**
   * Fudge field name.
   */
  private static final String SPECIFICATION_KEY = "specification";
  /**
   * Fudge field name.
   */
  private static final String VALUE_KEY = "value";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ComputedValue object) {
    MutableFudgeMsg msg = serializer.newMessage();
    ValueSpecification specification = object.getSpecification();
    if (specification != null) {
      serializer.addToMessage(msg, SPECIFICATION_KEY, null, specification);
    }
    Object value = object.getValue();
    if (value != null) {
      serializer.addToMessageWithClassHeaders(msg, VALUE_KEY, null, value);
    }
    return msg;
  }

  @Override
  public ComputedValue buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField fudgeField = message.getByName(SPECIFICATION_KEY);
    Validate.notNull(fudgeField, "Fudge message is not a ComputedValue - field 'specification' is not present");
    ValueSpecification valueSpec = deserializer.fieldValueToObject(ValueSpecification.class, fudgeField);
    fudgeField = message.getByName(VALUE_KEY);
    Validate.notNull(fudgeField, "Fudge message is not a ComputedValue - field 'value' is not present");
    Object valueObject = deserializer.fieldValueToObject(fudgeField);
    return new ComputedValue(valueSpec, valueObject);
  }

}
