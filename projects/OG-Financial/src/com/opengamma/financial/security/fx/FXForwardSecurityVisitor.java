/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fx;

/**
 * Visitor for the {@code FXForwardSecurity} type
 * 
 * @param <T> visitor method return type
 */
public interface FXForwardSecurityVisitor<T> {

  T visitFXForwardSecurity(FXForwardSecurity security);
}
