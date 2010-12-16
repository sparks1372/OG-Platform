/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.CounterpartyImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.FullPortfolioGetRequest;
import com.opengamma.master.position.ManageablePortfolio;
import com.opengamma.master.position.PortfolioTreeDocument;

/**
 * Tests QueryFullDbPositionMasterWorker.
 */
public class QueryFullDbPositionMasterWorkerGetFullPortfolioTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryFullDbPositionMasterWorkerGetFullPortfolioTest.class);

  private QueryFullDbPositionMasterWorker _worker;
  private DbPositionMasterWorker _queryWorker;
  private DbPositionMasterWorker _modifyTreeWorker;

  public QueryFullDbPositionMasterWorkerGetFullPortfolioTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryFullDbPositionMasterWorker();
    _worker.init(_posMaster);
    _queryWorker = new QueryPositionDbPositionMasterWorker();
    _queryWorker.init(_posMaster);
    _modifyTreeWorker = new ModifyPortfolioTreeDbPositionMasterWorker();
    _modifyTreeWorker.init(_posMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_getFullPortfolio_nullUID() {
    _worker.getFullPortfolio(null);
  }

  @Test
  public void test_getFullPortfolio_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0");
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(uid);
    Portfolio test = _worker.getFullPortfolio(request);
    assertNull(test);
  }

  @Test
  public void test_getFullPortfolio_101() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101");
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(uid);
    Portfolio test = _worker.getFullPortfolio(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "101"), test.getUniqueIdentifier().toLatest());
    assertEquals("TestPortfolio101", test.getName());
    PortfolioNode testRoot = test.getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "111"), testRoot.getUniqueIdentifier().toLatest());
    assertEquals("TestNode111", testRoot.getName());
    assertEquals(0, testRoot.getPositions().size());
    assertEquals(1, testRoot.getChildNodes().size());
    
    PortfolioNode testChild112 = testRoot.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "112"), testChild112.getUniqueIdentifier().toLatest());
    assertEquals("TestNode112", testChild112.getName());
    assertEquals(5, testChild112.getPositions().size());
    assertEquals(1, testChild112.getChildNodes().size());
    
    PortfolioNode testChild113 = testChild112.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "113"), testChild113.getUniqueIdentifier().toLatest());
    assertEquals("TestNode113", testChild113.getName());
    assertEquals(0, testChild113.getPositions().size());
    assertEquals(0, testChild113.getChildNodes().size());
    
    assertPortfolio112Positions(testChild112);
    
  }

  @Test
  public void test_getFullPortfolio_101_sameUidDifferentInstant() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101");
    Instant now = Instant.now(_posMaster.getTimeSource());
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(uid, now, now);
    Portfolio testNow = _worker.getFullPortfolio(request);
    
    Instant later = now.plusSeconds(100);
    FullPortfolioGetRequest requestLater = new FullPortfolioGetRequest(uid, later, later);
    Portfolio testLater = _worker.getFullPortfolio(requestLater);
    
    assertEquals(testLater.getUniqueIdentifier(), testNow.getUniqueIdentifier());
    assertEquals(testLater.getRootNode().getUniqueIdentifier(), testNow.getRootNode().getUniqueIdentifier());
    assertEquals(testLater.getRootNode().getChildNodes().get(0).getUniqueIdentifier(), testNow.getRootNode().getChildNodes().get(0).getUniqueIdentifier());
    assertEquals(testLater.getRootNode().getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier(), testNow.getRootNode().getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier());
  }

  @Test
  public void test_getFullPortfolio_latest_notLatest() {
    // latest
    Instant now = Instant.now(_posMaster.getTimeSource());
    FullPortfolioGetRequest requestLatest = new FullPortfolioGetRequest(UniqueIdentifier.of("DbPos", "201"), now, now);
    Portfolio testLatest = _worker.getFullPortfolio(requestLatest);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), testLatest.getUniqueIdentifier().toLatest());
    
    // earlier
    Instant earlier = _version1Instant.plusSeconds(5);
    FullPortfolioGetRequest requestEarlier = new FullPortfolioGetRequest(UniqueIdentifier.of("DbPos", "201"), earlier, earlier);
    Portfolio testEarlier = _worker.getFullPortfolio(requestEarlier);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), testEarlier.getUniqueIdentifier().toLatest());
    
    // ensure earlier is actually earlier
    assertTrue(testEarlier.getUniqueIdentifier().getVersion().compareTo(testLatest.getUniqueIdentifier().getVersion()) < 0);
  }

  @Test
  public void test_getFullPortfolio_byFullPortfolioUid_latest() {
    // not latest version
    Instant later = _version2Instant.plusSeconds(5);
    FullPortfolioGetRequest requestBase = new FullPortfolioGetRequest(UniqueIdentifier.of("DbPos", "201"), later, later);
    Portfolio testBase = _worker.getFullPortfolio(requestBase);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), testBase.getUniqueIdentifier().toLatest());
    
    // retrieve using full portfolio uid
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(testBase.getUniqueIdentifier());  // get using returned uid
    Portfolio test = _worker.getFullPortfolio(request);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), test.getUniqueIdentifier().toLatest());
    assertEquals(testBase.getUniqueIdentifier(), test.getUniqueIdentifier());
    assertEquals(Long.toHexString(_version2Instant.toEpochMillisLong()) + "-0", test.getUniqueIdentifier().getVersion());
  }

  @Test
  public void test_getFullPortfolio_byFullPortfolioUid_notLatest() {
    // not latest version
    Instant earlier = _version1Instant.plusSeconds(5);
    FullPortfolioGetRequest requestBase = new FullPortfolioGetRequest(UniqueIdentifier.of("DbPos", "201"), earlier, earlier);
    Portfolio testBase = _worker.getFullPortfolio(requestBase);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), testBase.getUniqueIdentifier().toLatest());
    
    // retrieve using full portfolio uid
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(testBase.getUniqueIdentifier());  // get using returned uid
    Portfolio test = _worker.getFullPortfolio(request);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), test.getUniqueIdentifier().toLatest());
    assertEquals(testBase.getUniqueIdentifier(), test.getUniqueIdentifier());
    assertEquals(Long.toHexString(_version1Instant.toEpochMillisLong()) + "-0", test.getUniqueIdentifier().getVersion());
  }

  @Test
  public void test_getFullPortfolio_noPositions() {
    PortfolioTreeDocument doc = _modifyTreeWorker.addPortfolioTree(new PortfolioTreeDocument(new ManageablePortfolio("Root")));
    
    // not latest version
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(doc.getUniqueId(), null, null);
    Portfolio test = _worker.getFullPortfolio(request);
    assertEquals(doc.getUniqueId().toLatest(), test.getUniqueIdentifier().toLatest());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }
  
  private void assertPortfolio112Positions(PortfolioNode node112) {
    
    Position testPos100 = node112.getPositions().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "100", "0"), testPos100.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(100.987), testPos100.getQuantity());
    IdentifierBundle testSecKey100 = testPos100.getSecurityKey();
    assertNotNull(testSecKey100);
    assertEquals(1, testSecKey100.size());
    assertEquals(true, testSecKey100.getIdentifiers().contains(Identifier.of("TICKER", "S100")));
    Set<Trade> trades100 = testPos100.getTrades();
    assertNotNull(trades100);
    assertTrue(trades100.isEmpty());
    
    Position testPos120 = node112.getPositions().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "120", "0"), testPos120.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(120.987), testPos120.getQuantity());
    IdentifierBundle testSecKey120 = testPos120.getSecurityKey();
    assertNotNull(testSecKey120);
    assertEquals(1, testSecKey120.size());
    assertEquals(true, testSecKey120.getIdentifiers().contains(Identifier.of("TICKER", "T130")));
    Collection<Trade> trades120 = testPos120.getTrades();
    assertNotNull(trades120);
    assertEquals(1, trades120.size());
    TradeImpl expectedTrade = new TradeImpl(testPos120.getUniqueIdentifier(), Identifier.of("TICKER", "T130"), BigDecimal.valueOf(120.987), 
        new CounterpartyImpl(Identifier.of("CPARTY", "C100")), _now.toLocalDate(), _now.toOffsetTime().minusSeconds(220));
    expectedTrade.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "400", "0"));
    assertEquals(expectedTrade, trades120.iterator().next());
    
    Position testPos121 = node112.getPositions().get(2);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), testPos121.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), testPos121.getQuantity());
    IdentifierBundle testSecKey121 = testPos121.getSecurityKey();
    assertNotNull(testSecKey121);
    assertEquals(2, testSecKey121.size());
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
    Set<Trade> trades121 = testPos121.getTrades();
    assertNotNull(trades121);
    assertEquals(1, trades121.size());
    expectedTrade = new TradeImpl(testPos121.getUniqueIdentifier(), testSecKey121, BigDecimal.valueOf(121.987), new CounterpartyImpl(Identifier.of("CPARTY", "C101")), _now.toLocalDate(), _now.toOffsetTime().minusSeconds(221));
    expectedTrade.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "401", "0"));
    assertEquals(expectedTrade, trades121.iterator().next());
    
    Position testPos122 = node112.getPositions().get(3);
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), testPos122.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(122.987), testPos122.getQuantity());
    IdentifierBundle testSecKey122 = testPos122.getSecurityKey();
    assertNotNull(testSecKey122);
    assertEquals(1, testSecKey122.size());
    assertEquals(Identifier.of("TICKER", "ORCL"), testSecKey122.getIdentifiers().iterator().next());
    Set<Trade> trades122 = testPos122.getTrades();
    assertNotNull(trades122);
    assertEquals(2, trades122.size());
        
    expectedTrade = new TradeImpl(testPos122.getUniqueIdentifier(), testSecKey122, BigDecimal.valueOf(22.987), 
        new CounterpartyImpl(Identifier.of("CPARTY", "CISC")), _now.toLocalDate(), _now.toOffsetTime().minusSeconds(222));
    expectedTrade.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "403", "0"));
    assertTrue(trades122.contains(expectedTrade));
    
    expectedTrade = new TradeImpl(testPos122.getUniqueIdentifier(), testSecKey122, BigDecimal.valueOf(100.987), 
        new CounterpartyImpl(Identifier.of("CPARTY", "JMP")), _now.toLocalDate(), _now.toOffsetTime().minusSeconds(222));
    expectedTrade.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "402", "0"));
    assertTrue(trades122.contains(expectedTrade));
    
    Position testPos123 = node112.getPositions().get(4);
    assertEquals(UniqueIdentifier.of("DbPos", "123", "0"), testPos123.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(123.987), testPos123.getQuantity());
    IdentifierBundle testSecKey123 = testPos123.getSecurityKey();
    assertNotNull(testSecKey123);
    assertEquals(2, testSecKey123.size());
    assertTrue(testSecKey123.getIdentifiers().contains(Identifier.of("TICKER", "ORCL134")));
    assertTrue(testSecKey123.getIdentifiers().contains(Identifier.of("NASDAQ", "ORCL135")));
    Set<Trade> trades123 = testPos123.getTrades();
    assertNotNull(trades123);
    assertEquals(3, trades123.size());
    
    List<TradeImpl> expectedTrades = Lists.newArrayList();
    expectedTrades.add(new TradeImpl(testPos123.getUniqueIdentifier(), testSecKey123, BigDecimal.valueOf(100.987), 
        new CounterpartyImpl(Identifier.of("CPARTY", "C104")), _now.toLocalDate(), _now.toOffsetTime().minusSeconds(223)));
    expectedTrades.get(0).setUniqueIdentifier(UniqueIdentifier.of("DbPos", "404", "0"));
    
    expectedTrades.add(new TradeImpl(testPos123.getUniqueIdentifier(), testSecKey123, BigDecimal.valueOf(200.987), 
        new CounterpartyImpl(Identifier.of("CPARTY", "C105")), _now.toLocalDate(), _now.toOffsetTime().minusSeconds(223)));
    expectedTrades.get(1).setUniqueIdentifier(UniqueIdentifier.of("DbPos", "405", "0"));
    
    expectedTrade = new TradeImpl(testPos123.getUniqueIdentifier(), testSecKey123, BigDecimal.valueOf(300.987), new CounterpartyImpl(Identifier.of("CPARTY", "C106")), _now.toLocalDate(), _now.toOffsetTime().minusSeconds(223));
    expectedTrade.setUniqueIdentifier(UniqueIdentifier.of("DbPos", "406", "0"));
    assertTrue(trades123.contains(expectedTrade));
  }

}