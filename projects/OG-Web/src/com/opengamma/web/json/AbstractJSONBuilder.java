/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.io.StringReader;
import java.io.StringWriter;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.json.FudgeJSONStreamReader;
import org.fudgemsg.wire.json.FudgeJSONStreamWriter;
import org.json.JSONException;
import org.json.JSONObject;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;


/**
 * Partial implementation of {@link JSONBuilder}
 */
/* package */abstract class AbstractJSONBuilder<T> implements JSONBuilder<T> {
  
  protected static final String FUDGE_ENVELOPE_FIELD = "fudgeEnvelope";
  protected static final String NAME_FIELD = "name";
  protected static final String UNIQUE_ID_FIELD = "uniqueId";
  
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  private final FudgeSerializer _serializer = new FudgeSerializer(_fudgeContext);
  private final FudgeDeserializer _deserializer = new FudgeDeserializer(_fudgeContext);

  protected FudgeMsg removeRedundantFields(final FudgeMsg message) {
    MutableFudgeMsg result = _fudgeContext.newMessage();
    for (FudgeField fudgeField : message) {
      if (fudgeField.getName() != null || (fudgeField.getOrdinal() != null && fudgeField.getOrdinal() != FudgeSerializer.TYPES_HEADER_ORDINAL)) {
        if (fudgeField.getValue() instanceof FudgeMsg) {
          FudgeMsg subMsg = (FudgeMsg) fudgeField.getValue();
          subMsg = removeRedundantFields(subMsg);
          result.add(fudgeField.getName(), subMsg);
        } else {
          result.add(fudgeField);
        }
      }
    }
    return result;
  }
  
  protected <E> E convertJsonToObject(Class<E> clazz, JSONObject json) {
    FudgeMsg fudgeMsg = convertJSONToFudgeMsg(json);
    return _deserializer.fudgeMsgToObject(clazz, fudgeMsg);
  }

  protected FudgeMsg convertJSONToFudgeMsg(final JSONObject jsonDoc) {
    final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeJSONStreamReader(_fudgeContext, new StringReader(jsonDoc.toString())));
    return fmr.nextMessage();
  }
  
  protected JSONObject toJSONObject(Object obj) throws JSONException {
    return toJSONObject(obj, true);
  }

  protected JSONObject toJSONObject(Object obj, boolean removeRedundantFields) throws JSONException {
    if (obj == null) {
      return null;
    }
    MutableFudgeMsg fudgeMsg = _serializer.objectToFudgeMsg(obj);
    StringWriter buf = new StringWriter(1024);  
    FudgeMsgWriter writer = new FudgeMsgWriter(new FudgeJSONStreamWriter(_fudgeContext, buf));
    if (removeRedundantFields) {
      writer.writeMessage(removeRedundantFields(fudgeMsg));
    } else {
      writer.writeMessage(fudgeMsg);
    }
    
    return new JSONObject(buf.toString());
  }
}
