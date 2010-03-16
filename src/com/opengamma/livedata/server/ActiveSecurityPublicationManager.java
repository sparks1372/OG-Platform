/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecificationImpl;
import com.opengamma.livedata.client.HeartbeatSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Keeps track of all securities currently being published, and controls the
 * expiry by keeping track of heartbeat messages.
 *
 * @author kirk
 */
public class ActiveSecurityPublicationManager implements SubscriptionListener {
  public static final long DEFAULT_TIMEOUT_EXTENSION = 3 * HeartbeatSender.DEFAULT_PERIOD;
  public static final long DEFAULT_CHECK_PERIOD = HeartbeatSender.DEFAULT_PERIOD / 2;
  private static final Logger s_logger = LoggerFactory.getLogger(ActiveSecurityPublicationManager.class);
  // Injected Inputs:
  private final AbstractLiveDataServer _dataServer;
  private final long _timeoutExtension;
  
  public ActiveSecurityPublicationManager(AbstractLiveDataServer dataServer) {
    this(dataServer, DEFAULT_CHECK_PERIOD);
  }
  
  public ActiveSecurityPublicationManager(AbstractLiveDataServer dataServer, long checkPeriod) {
    this(dataServer, DEFAULT_TIMEOUT_EXTENSION, new Timer("ActiveSecurityPublicationManager Timer"), checkPeriod);
  }
  
  public ActiveSecurityPublicationManager(AbstractLiveDataServer dataServer, long timeoutExtension, long checkPeriod) {
    this(dataServer, timeoutExtension, new Timer("ActiveSecurityPublicationManager Timer"), checkPeriod);
  }
  
  public ActiveSecurityPublicationManager(AbstractLiveDataServer dataServer, long timeoutExtension, Timer timer, long checkPeriod) {
    ArgumentChecker.checkNotNull(dataServer, "Data Server");
    ArgumentChecker.checkNotNull(timer, "Expiration Timer");
    _dataServer = dataServer;
    _timeoutExtension = timeoutExtension;
    _dataServer.addSubscriptionListener(this);
    timer.schedule(new ExpirationCheckTimerTask(), checkPeriod, checkPeriod);
  }

  /**
   * @return the dataServer
   */
  public AbstractLiveDataServer getDataServer() {
    return _dataServer;
  }

  /**
   * @return the timeoutExtension
   */
  public long getTimeoutExtension() {
    return _timeoutExtension;
  }
  
  @Override
  public void subscribed(Subscription subscription) {
    extendPublicationTimeout(subscription);
  }
  
  @Override
  public void madePersistent(Subscription subscription) {
  }

  @Override
  public void unsubscribed(Subscription subscription) {
  }

  public void extendPublicationTimeout(LiveDataSpecificationImpl spec) {
    Subscription subscription = _dataServer.getSubscription(spec);
    if (subscription != null) {
      extendPublicationTimeout(subscription);
    }
  }

  private void extendPublicationTimeout(Subscription subscription) {
    synchronized (subscription) {
      if (!subscription.isPersistent()) {
        subscription.setExpiry(System.currentTimeMillis() + getTimeoutExtension());
      }
    }
  }
  
  public class ExpirationCheckTimerTask extends TimerTask {
    @Override
    public void run() {
      try {
        expirationCheck();
      } catch (RuntimeException e) {
        s_logger.error("Checking for data specifications to time out failed", e);
      }
    }
  }
    
  private void expirationCheck() {
    s_logger.debug("Checking for data specifications to time out");
    int nExpired = 0;
    long startTime = System.currentTimeMillis();
    for (Subscription subscription : _dataServer.getSubscriptions()) {
      // TODO Move this logic to Subscription.hasExpired()
      if (!subscription.isPersistent()) {
        if (subscription.getExpiry() < startTime) {
          boolean removed = getDataServer().unsubscribe(subscription);
          if (removed) {
            nExpired++;
          }
        }
      }
    }
    s_logger.info("Expired {} specifications", nExpired);
  }

}
