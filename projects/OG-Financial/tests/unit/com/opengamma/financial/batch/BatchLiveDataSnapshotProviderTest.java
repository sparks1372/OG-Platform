/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import com.opengamma.core.historicaltimeseries.impl.MockHistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.HistoricalMarketDataProvider;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.batch.marketdata.BatchMarketDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * 
 */
public class BatchLiveDataSnapshotProviderTest {
  
  @Test
  public void basicCase() {
    final LocalDate date = LocalDate.of(2005, 11, 12);
    
    ArrayLocalDateDoubleTimeSeries timeSeries = new ArrayLocalDateDoubleTimeSeries(
        new LocalDate[] { date },
        new double[] { 11.12 });
    
    CommandLineBatchJob job = new CommandLineBatchJob();
    job.getParameters().initializeDefaults(job);
    CommandLineBatchJobRun run = new CommandLineBatchJobRun(job,
        date,
        date,
        date,
        date);
    job.getParameters().setSnapshotObservationTime("LDN_CLOSE");
    
    MockHistoricalTimeSeriesSource historicalSource = new MockHistoricalTimeSeriesSource();
    
    ExternalId identifier = ExternalId.of("mytimeseries", "500");
    ExternalIdBundle bundle = ExternalIdBundle.of(identifier);
    historicalSource.storeHistoricalTimeSeries(bundle, "BLOOMBERG", "CMPL", "PX_LAST", timeSeries);
    
    HistoricalMarketDataProvider snapshotProvider = new HistoricalMarketDataProvider(historicalSource, "BLOOMBERG", "CMPL", "PX_LAST");
    InMemoryLKVMarketDataProvider batchDbProvider = new InMemoryLKVMarketDataProvider();
    
    BatchMarketDataProvider provider = new BatchMarketDataProvider(run, new DummyBatchMaster(), batchDbProvider, snapshotProvider);
    
    HistoricalMarketDataSpecification marketDataSpec = new HistoricalMarketDataSpecification(LocalDate.of(2005, 11, 12).atStartOfDayInZone(TimeZone.UTC), "BLOOMBERG", "CMPL", "PX_LAST");
    MarketDataSnapshot snapshot = provider.snapshot(marketDataSpec);
    snapshot.init();
    Object ts = snapshot.query(new ValueRequirement("foo", identifier));
    assertEquals(11.12, ts);
    
    assertNull(snapshot.query(new ValueRequirement("foo", ExternalId.of("mytimeseries2", "500"))));
    assertNull(snapshot.query(new ValueRequirement("foo", ExternalId.of("mytimeseries", "501"))));
  }

}
