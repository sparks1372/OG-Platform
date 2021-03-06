/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

/**
 * A manager handling the storage of ad hoc batches.
 * <p>
 * An ad hoc batch is a batch that is created by the user in a tool such as Excel.
 * If the user likes the results from their ad hoc batch enough, they may choose to 
 * persist them in batch DB. 
 */
public interface AdHocBatchDbManager {

  /**
   * Writes ad hoc batch result into batch DB.
   * <p>
   * The risk is written into the database in one fell swoop. 
   *   
   * @param batch  the result to be written, not null
   */
  void write(AdHocBatchResult batch);

}
