/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.time.Instant;
import javax.time.calendar.OffsetDateTime;

import org.junit.Test;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.CounterpartyImpl;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.core.security.test.MockSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.FullPortfolioGetRequest;
import com.opengamma.master.position.FullPortfolioNodeGetRequest;
import com.opengamma.master.position.FullPositionGetRequest;
import com.opengamma.master.position.FullTradeGetRequest;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.MasterPositionSource;

/**
 * Test MasterPositionSource.
 */
public class MasterPositionSourceTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("A", "B");

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterPositionSource(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterPositionSource(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor3arg_nullMaster() throws Exception {
    new MasterPositionSource(null, null, null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getPortfolio() throws Exception {
    PositionMaster mock = mock(PositionMaster.class);
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(UID);
    Instant now = Instant.now();
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    Portfolio portfolio = new PortfolioImpl(UID, "Hello");
    
    when(mock.getFullPortfolio(request)).thenReturn(portfolio);
    MasterPositionSource test = new MasterPositionSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Portfolio testResult = test.getPortfolio(UID);
    verify(mock, times(1)).getFullPortfolio(request);
    
    assertEquals(UID, testResult.getUniqueIdentifier());
    assertEquals("Hello", testResult.getName());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getPortfolioNode() throws Exception {
    PositionMaster mock = mock(PositionMaster.class);
    FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(UID);
    Instant now = Instant.now();
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    PortfolioNode node = new PortfolioNodeImpl(UID, "Hello");
    
    when(mock.getFullPortfolioNode(request)).thenReturn(node);
    MasterPositionSource test = new MasterPositionSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    PortfolioNode testResult = test.getPortfolioNode(UID);
    verify(mock, times(1)).getFullPortfolioNode(request);
    
    assertEquals(UID, testResult.getUniqueIdentifier());
    assertEquals("Hello", testResult.getName());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getPosition() throws Exception {
    PositionMaster mock = mock(PositionMaster.class);
    FullPositionGetRequest request = new FullPositionGetRequest(UID);
    Instant now = Instant.now();
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    Position node = new PositionImpl(UID, BigDecimal.TEN, Identifier.of("B", "C"));
    
    when(mock.getFullPosition(request)).thenReturn(node);
    MasterPositionSource test = new MasterPositionSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Position testResult = test.getPosition(UID);
    verify(mock, times(1)).getFullPosition(request);
    
    assertEquals(UID, testResult.getUniqueIdentifier());
    assertEquals(BigDecimal.TEN, testResult.getQuantity());
    assertEquals(Identifier.of("B", "C"), testResult.getSecurityKey().getIdentifiers().iterator().next());
  }
  
  @Test
  public void test_getTrade() throws Exception {
    PositionMaster mock = mock(PositionMaster.class);
    FullTradeGetRequest request = new FullTradeGetRequest(UID);

    OffsetDateTime now = OffsetDateTime.now();
    final UniqueIdentifier positionId = UniqueIdentifier.of("P", "A");
    final MockSecurity security = new MockSecurity("A");
    security.setIdentifiers(IdentifierBundle.of(Identifier.of("S", "A")));
    final Counterparty counterparty = new CounterpartyImpl(Identifier.of("CPARTY", "C100"));
    
    TradeImpl trade = new TradeImpl(positionId, security, BigDecimal.TEN, counterparty, now.toLocalDate(), now.toOffsetTime().minusSeconds(100));
    trade.setUniqueIdentifier(UID);
    
    when(mock.getFullTrade(request)).thenReturn(trade);
    
    MasterPositionSource test = new MasterPositionSource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Trade testResult = test.getTrade(UID);
    verify(mock, times(1)).getFullTrade(request);
    
    assertEquals(UID, testResult.getUniqueIdentifier());
    assertEquals(BigDecimal.TEN, testResult.getQuantity());
    assertEquals(Identifier.of("S", "A"), testResult.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals(counterparty, testResult.getCounterparty());
    assertEquals(positionId, testResult.getPositionId());
    assertEquals(now.toLocalDate(), testResult.getTradeDate());
  }

}