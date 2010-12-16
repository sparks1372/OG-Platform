/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Test DbSecurityMaster.
 */
public class DbSecurityMasterTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DbSecurityMasterTest.class);

  private DbSecurityMaster _secMaster;

  public DbSecurityMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _secMaster = (DbSecurityMaster) context.getBean(getDatabaseType() + "DbSecurityMaster");
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _secMaster = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_basics() throws Exception {
    assertNotNull(_secMaster);
    assertEquals(true, _secMaster.getIdentifierScheme().equals("DbSec"));
    assertNotNull(_secMaster.getDbSource());
    assertNotNull(_secMaster.getTimeSource());
    assertNotNull(_secMaster.getWorkers());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equity() throws Exception {
    EquitySecurity sec = new EquitySecurity("London", "LON", "OpenGamma Ltd", Currency.getInstance("GBP"));
    sec.setName("OpenGamma");
    sec.setGicsCode(GICSCode.getInstance(2));
    sec.setShortName("OG");
    sec.setIdentifiers(IdentifierBundle.of(Identifier.of("Test", "OG")));
    SecurityDocument addDoc = new SecurityDocument(sec);
    SecurityDocument added = _secMaster.add(addDoc);
    
    SecurityDocument loaded = _secMaster.get(added.getUniqueId());
    assertEquals(added, loaded);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals("DbSecurityMaster[DbSec]", _secMaster.toString());
  }

}