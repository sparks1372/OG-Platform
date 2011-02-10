/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

/**
 * This enum specifies whether a new run should be created in the batch database 
 * when a batch run is started - or, more importantly, restarted.
 */ 
public enum RunCreationMode {
  /**
   * Automatic mode.
   * <p>
   * When a batch run is started, the system will try to find an existing
   * run in the database with the same run date and observation time
   * (for example, 20101105/LDN_CLOSE). If such a run is found,
   * the system checks that all {@link BatchJobParameters} match
   * with the previous run. If all parameters match, the run is reused.
   * Otherwise, an error is thrown.
   * <p>
   * Only {@link BatchJobParameters} are checked. Variables 
   * like OpenGamma version and master process host ID can change
   * between run attempts and this will not prevent the run
   * from being reused.  
   * <p>
   * Reusing the run means that the system checks what risk figures
   * are already in the database for that run. The system will then try 
   * calculate any missing risk. Conversely, not reusing the run means 
   * that all risk is calculated from scratch. 
   */
  AUTO,
  
  /**
   * Always mode.
   * <p>  
   * When a batch run is started, the system will always create a new run
   * in the database. It will not try to find an existing run in the database.
   * <p>
   * An error is thrown if there is already a run with the same date and observation
   * time in the database.
   */
  ALWAYS,
  
  /**
   * Never mode.
   * <p>
   * When a batch run is started, the the system will try to find an existing
   * run in the database with the same run date and observation time
   * (for example, 20101105/LDN_CLOSE). If no such run is found,
   * or if there is more than one such run,
   * an error is reported and the process quits. If a unique matching run is found, 
   * however, that run is used no
   * matter if the parameters used to start that run were the same
   * as the parameters used to start the current run.
   * The user takes responsibility for any inconsistencies or errors
   * that may result from this.
   * <p>
   * This mode may be useful in rare error situations where the user needs to modify
   * the parameters to make the run complete. It should not normally be used.
   */
  NEVER
}