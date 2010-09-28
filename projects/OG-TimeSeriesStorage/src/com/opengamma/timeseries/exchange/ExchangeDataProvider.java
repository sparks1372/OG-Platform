/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.exchange;

/**
 * 
 *
 * @author yomi
 */
public interface ExchangeDataProvider {
 
  Exchange getExchange(String micCode);
}