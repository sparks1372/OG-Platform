/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.monitor;

import org.slf4j.Logger;

/**
 * 
 *
 * @author kirk
 */
public class LoggingOperationTimeReporter implements OperationTimeReporter {

  @Override
  public void report(long duration, Logger logger, String format,
      Object[] arguments) {
    
    String newFormat = "{}ms-" + format;
    Object[] newArgs = new Object[arguments.length + 1];
    newArgs[0] = duration;
    System.arraycopy(arguments, 0, newArgs, 1, arguments.length);
    logger.info(newFormat, newArgs);
  }

}