/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Fudge builder for {@code ZonedDateTime}.
 */
@FudgeBuilderFor(ZonedDateTime.class)
public final class ZonedDateTimeBuilder implements FudgeBuilder<ZonedDateTime> {

  /** Field name. */
  public static final String DATETIME_FIELD_NAME = "datetime";
  /** Field name. */
  public static final String ZONE_FIELD_NAME = "zone";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ZonedDateTime object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(DATETIME_FIELD_NAME, object.toOffsetDateTime());
    msg.add(ZONE_FIELD_NAME, object.getZone().getID());
    return msg;
  }

  @Override
  public ZonedDateTime buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final OffsetDateTime odt = msg.getValue(OffsetDateTime.class, DATETIME_FIELD_NAME);
    if (odt == null) {
      throw new IllegalArgumentException("Fudge message is not a ZonedDateTime - field 'datetime' is not present");
    }
    final String zone = msg.getString(ZONE_FIELD_NAME);
    if (zone == null) {
      throw new IllegalArgumentException("Fudge message is not a ZonedDateTime - field 'zone' is not present");
    }
    return ZonedDateTime.of(odt, TimeZone.of(zone));
  }

}
