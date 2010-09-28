/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;

/**
 * 
 *
 */
public class MockLiveDataServerTest {
  
  private IdentificationScheme _domain;
  private MockLiveDataServer _server;
  
  @Before
  public void setUp() {
    _domain = new IdentificationScheme("test");
    _server = new MockLiveDataServer(_domain);
    _server.connect();
  }
  
  @Test
  public void persistentSubscription() {
    getMethods("persistent", true);
  }
  
  @Test
  public void nonpersistentSubscription() {
    getMethods("nonpersistent", false);
  }
  
  private LiveDataSpecification getSpec(String uniqueId) {
    LiveDataSpecification spec = new LiveDataSpecification(
        _server.getDefaultNormalizationRuleSetId(),
        new Identifier(_server.getUniqueIdDomain(), uniqueId));
    return spec;
  }
  
  private void getMethods(String uniqueId, boolean persistent) {
    LiveDataSpecification spec = getSpec(uniqueId);    
    
    SubscriptionResult result = _server.subscribe(uniqueId, persistent);

    assertNotNull(result);
    assertTrue(result.getResult() == LiveDataSubscriptionResult.SUCCESS);
    
    DistributionSpecification distributionSpec = result.getDistributionSpecification();
    assertNotNull(distributionSpec);
    
    
    Subscription subscription = _server.getSubscription(uniqueId); 
    
    assertNotNull(subscription);
    assertEquals(uniqueId, subscription.getSecurityUniqueId());
    assertEquals(1, subscription.getDistributors().size());
    assertSame(subscription, _server.getSubscription(spec));
    
    assertTrue(_server.isSubscribedTo(subscription));
    assertFalse(_server.isSubscribedTo(new Subscription("foo", _server.getMarketDataSenderFactory())));
    assertTrue(_server.isSubscribedTo(uniqueId));
    assertFalse(_server.isSubscribedTo("foo"));
    assertTrue(_server.isSubscribedTo(spec));
    assertFalse(_server.isSubscribedTo(getSpec("foo")));
    
    assertEquals(1, _server.getSubscriptions().size());
    assertEquals(1, _server.getNumActiveSubscriptions());
    assertSame(subscription, _server.getSubscriptions().iterator().next());
    assertEquals(1, _server.getActiveSubscriptionIds().size());
    assertEquals(uniqueId, _server.getActiveSubscriptionIds().iterator().next());
    
    assertEquals(0, _server.getNumLiveDataUpdatesSentPerSecondOverLastMinute(), 0.0001);
    assertEquals(0, _server.getNumMarketDataUpdatesReceived());
    
    MarketDataDistributor distributor = subscription.getDistributors().iterator().next();
        
    assertSame(distributor, subscription.getMarketDataDistributor(spec));
    assertSame(distributor, subscription.getMarketDataDistributor(distributionSpec));
    
    assertSame(distributor, _server.getMarketDataDistributor(spec));
    assertSame(distributor, _server.getMarketDataDistributor(distributionSpec));
    
    assertTrue(distributor.isPersistent() == persistent);
    assertNull(distributor.getExpiry());
  }

  @Test
  public void subscribeUnsubscribeA() {
    _server.subscribe("nonpersistent", false);
    _server.subscribe("persistent", true);
    
    assertTrue(_server.unsubscribe("nonpersistent"));
    assertTrue(_server.unsubscribe("persistent"));
    
    assertNull(_server.getSubscription("nonpersistent"));
    assertNull(_server.getSubscription("persistent"));
    
    assertFalse(_server.isSubscribedTo("nonpersistent"));
    assertFalse(_server.isSubscribedTo("persistent"));
  }
  
  @Test
  public void subscribeUnsubscribeB() {
    _server.subscribe("nonpersistent", false);
    _server.subscribe("persistent", true);
    
    Subscription nonpersistent = _server.getSubscription("nonpersistent"); 
    Subscription persistent = _server.getSubscription("persistent");
    
    assertTrue(_server.unsubscribe(nonpersistent));
    assertTrue(_server.unsubscribe(persistent));  
  }
  
  @Test
  public void subscribeUnsubscribeC() {
    UserPrincipal user = new UserPrincipal("mark", "1.1.1.1");
    
    LiveDataSpecification requestedSpec = new LiveDataSpecification(
        StandardRules.getNoNormalization().getId(), 
        new Identifier(_domain, "testsub"));
    
    LiveDataSubscriptionRequest request = new LiveDataSubscriptionRequest(
        user,
        SubscriptionType.NON_PERSISTENT, 
        Collections.singleton(requestedSpec));
    
    LiveDataSubscriptionResponseMsg response = _server.subscriptionRequestMade(request);
    
    checkResponse(user, requestedSpec, response);
    
    assertTrue(_server.unsubscribe("testsub"));
    
    response = _server.subscriptionRequestMade(request);
    checkResponse(user, requestedSpec, response);
    
    assertTrue(_server.unsubscribe("testsub"));
  }
  
  @Test
  public void subscribeThenStopDistributor() {
    _server.subscribe("mysub", false);
    _server.subscribe("mysub", false);
    _server.subscribe("mysub", true);
    
    assertEquals(1, _server.getNumActiveSubscriptions());
    
    Subscription sub = _server.getSubscription("mysub");
    assertEquals(1, sub.getDistributors().size());

    LiveDataSpecification spec = getSpec("mysub");
    MarketDataDistributor distributor = _server.getMarketDataDistributor(spec);
    assertNotNull(distributor);

    assertFalse(_server.stopDistributor(distributor));
    distributor.setPersistent(false);
    assertTrue(_server.stopDistributor(distributor));
    assertTrue(sub.getDistributors().isEmpty());
    assertFalse(_server.isSubscribedTo("mysub"));
    assertNull(_server.getSubscription("mysub"));
    assertNull(_server.getSubscription(spec));
    assertNull(_server.getMarketDataDistributor(spec));
    assertEquals(0, _server.getNumActiveSubscriptions());
    
    assertFalse(_server.stopDistributor(distributor));
  }

  private void checkResponse(UserPrincipal user, LiveDataSpecification requestedSpec,
      LiveDataSubscriptionResponseMsg response) {
    assertEquals(user, response.getRequestingUser());
    assertEquals(1, response.getResponses().size());
    assertEquals(requestedSpec, response.getResponses().get(0).getRequestedSpecification());
    assertEquals(requestedSpec, response.getResponses().get(0).getFullyQualifiedSpecification()); // mock server does not modify spec
    assertEquals(LiveDataSubscriptionResult.SUCCESS, response.getResponses().get(0).getSubscriptionResult());
    assertEquals(null, response.getResponses().get(0).getSnapshot()); 
    assertEquals(requestedSpec.getIdentifiers().toString(), response.getResponses().get(0).getTickDistributionSpecification());
    assertEquals(null, response.getResponses().get(0).getUserMessage());
  }
  
  @Test
  public void snapshot() {
    UserPrincipal user = new UserPrincipal("mark", "1.1.1.1");
    
    LiveDataSpecification requestedSpec = new LiveDataSpecification(
        StandardRules.getNoNormalization().getId(), 
        new Identifier(_domain, "testsub"));
    
    LiveDataSubscriptionRequest request = new LiveDataSubscriptionRequest(
        user,
        SubscriptionType.SNAPSHOT, 
        Collections.singleton(requestedSpec));
    
    LiveDataSubscriptionResponseMsg response = _server.subscriptionRequestMade(request);
    
    assertEquals(user, response.getRequestingUser());
    assertEquals(1, response.getResponses().size());
    assertEquals(requestedSpec, response.getResponses().get(0).getRequestedSpecification());
    assertNull(response.getResponses().get(0).getFullyQualifiedSpecification());
    assertEquals(LiveDataSubscriptionResult.INTERNAL_ERROR, response.getResponses().get(0).getSubscriptionResult());
    assertNull(response.getResponses().get(0).getSnapshot()); 
    assertNull(response.getResponses().get(0).getTickDistributionSpecification());
    assertEquals("When snapshot for testsub was run through normalization, the message disappeared. " +
    		"This indicates there are buggy normalization rules in place, or that buggy (or unexpected) data was " +
    		"received from the underlying market data API. Check your normalization rules. " +
    		"Raw, unnormalized msg = FudgeMsg[]",
    		response.getResponses().get(0).getUserMessage());
    
    assertFalse(_server.unsubscribe("testsub"));
  }
  
}