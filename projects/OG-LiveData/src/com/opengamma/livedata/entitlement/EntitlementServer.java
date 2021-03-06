/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import java.util.ArrayList;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.EntitlementRequest;
import com.opengamma.livedata.msg.EntitlementResponse;
import com.opengamma.livedata.msg.EntitlementResponseMsg;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Server managing entitlements.
 * <p>
 * This receives {@link EntitlementRequest} requests, passing them onto a delegate
 * {@link LiveDataEntitlementChecker}, and returning {@link EntitlementResponseMsg} responses.
 */
public class EntitlementServer implements FudgeRequestReceiver {
  
  private static final Logger s_logger = LoggerFactory.getLogger(EntitlementServer.class);
  private final LiveDataEntitlementChecker _delegate;
  
  public EntitlementServer(LiveDataEntitlementChecker delegate) {
    ArgumentChecker.notNull(delegate, "Delegate entitlement checker");
    _delegate = delegate;
  }
  
  @Override
  @Transactional
  public FudgeMsg requestReceived(FudgeDeserializer deserializer, FudgeMsgEnvelope requestEnvelope) {
    FudgeMsg requestFudgeMsg = requestEnvelope.getMessage();
    EntitlementRequest entitlementRequest = EntitlementRequest.fromFudgeMsg(deserializer, requestFudgeMsg);
    s_logger.debug("Received entitlement request {}", entitlementRequest);
    
    Map<LiveDataSpecification, Boolean> isEntitledMap = _delegate.isEntitled(entitlementRequest.getUser(), entitlementRequest.getLiveDataSpecifications());
    
    ArrayList<EntitlementResponse> responses = new ArrayList<EntitlementResponse>();
    for (LiveDataSpecification spec : entitlementRequest.getLiveDataSpecifications()) {
      boolean isEntitled = isEntitledMap.get(spec);
      EntitlementResponse response;
      if (isEntitled) {
        response = new EntitlementResponse(spec, true);
      } else {
        response = new EntitlementResponse(spec, false, entitlementRequest.getUser() + " is not entitled to " + spec);
      }
      responses.add(response);
    }
    
    EntitlementResponseMsg response = new EntitlementResponseMsg(responses);
    FudgeMsg responseFudgeMsg = response.toFudgeMsg(new FudgeSerializer(deserializer.getFudgeContext()));
    return responseFudgeMsg;
  }
  
}
