/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import com.opengamma.masterdb.security.hibernate.EnumBean;

/**
 * Hibernate bean for storage.
 */
public class CashRateTypeBean extends EnumBean {
  
  protected CashRateTypeBean() {
  }

  public CashRateTypeBean(String cashRateType) {
    super(cashRateType);
  }
  
}
