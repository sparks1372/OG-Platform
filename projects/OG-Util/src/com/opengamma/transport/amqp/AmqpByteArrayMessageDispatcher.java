/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AmqpByteArrayMessageDispatcher implements MessageListener {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AmqpByteArrayMessageDispatcher.class);
  private final ByteArrayMessageReceiver _underlying;
  
  public AmqpByteArrayMessageDispatcher(ByteArrayMessageReceiver underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  /**
   * @return the underlying
   */
  public ByteArrayMessageReceiver getUnderlying() {
    return _underlying;
  }

  @Override
  public void onMessage(Message message) {
    byte[] bytes = message.getBody();
    s_logger.debug("Dispatching byte array of length {}", bytes.length);
    getUnderlying().messageReceived(bytes);
  }

}