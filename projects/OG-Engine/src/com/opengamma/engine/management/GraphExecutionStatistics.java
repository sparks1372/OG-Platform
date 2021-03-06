/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.sf.ehcache.CacheException;

import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calc.stats.TotallingGraphStatisticsGathererProvider.Statistics;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * An MBean implementation for those attributes and operations we wish to expose on a {@link com.opengamma.engine.view.calc.stats.GraphExecutionStatistics}.
 */
public class GraphExecutionStatistics implements GraphExecutionStatisticsMBean {
  
  private final TotallingGraphStatisticsGathererProvider _statisticsProvider;
  
  private final UniqueId _viewProcessId;
  
  private final String _viewDefinitionName;
  
  private final String _calcConfigName;

  private final ObjectName _objectName;
  
  /**
   * Create a management GraphExecutionStatistics
   * 
   * @param viewProcess  the view process
   * @param statisticsProvider  the statistics provider
   * @param viewProcessorId  the view processor identifier
   * @param calcConfigName  the calculation configuration name
   */
  public GraphExecutionStatistics(ViewProcess viewProcess, TotallingGraphStatisticsGathererProvider statisticsProvider, UniqueId viewProcessorId, String calcConfigName) {
    ArgumentChecker.notNull(statisticsProvider, "TotallingGraphStatisticsGathererProvider");
    ArgumentChecker.notNull(viewProcessorId, "viewProcessorId");
    ArgumentChecker.notNull(viewProcess, "View Process");
    ArgumentChecker.notNull(calcConfigName, "calcConfig Name");
    _viewProcessId = viewProcess.getUniqueId();
    _viewDefinitionName = viewProcess.getDefinitionName();
    _objectName = createObjectName(viewProcessorId, _viewProcessId, calcConfigName);
    _calcConfigName = calcConfigName;
    _statisticsProvider = statisticsProvider;
  }

  /**
   * Creates an object name using the scheme "com.opengamma:type=GraphExecutionStatistics,ViewProcessor=<viewProcessorName>,View=<viewName>,name=<calcConfigName>"
   */
  static ObjectName createObjectName(UniqueId viewProcessorId, UniqueId viewProcessId, String calcConfigName) {
    ObjectName objectName;
    try {
      objectName = new ObjectName("com.opengamma:type=GraphExecutionStatistics,ViewProcessor=ViewProcessor " + viewProcessorId.getValue()
          + ",ViewProcess=ViewProcess " + viewProcessId.getValue() + ",name=" + calcConfigName);
    } catch (MalformedObjectNameException e) {
      throw new CacheException(e);
    }
    return objectName;
  }

  @Override
  public UniqueId getViewProcessId() {
    return _viewProcessId;
  }
  
  @Override
  public String getViewDefinitionName() {
    return _viewDefinitionName;
  }

  @Override
  public String getCalcConfigName() {
    return _calcConfigName;
  }

  @Override
  public Long getProcessedGraphs() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedGraphs() : 0; 
  }
  
  private com.opengamma.engine.view.calc.stats.GraphExecutionStatistics getGraphExecutionStatistics() {
    Statistics statisticsGatherer = _statisticsProvider.getStatisticsGatherer(_viewProcessId);
    List<com.opengamma.engine.view.calc.stats.GraphExecutionStatistics> executionStatistics = statisticsGatherer.getExecutionStatistics();
    for (com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics : executionStatistics) {
      if (graphExecutionStatistics.getCalcConfigName().equals(_calcConfigName) && graphExecutionStatistics.getViewProcessId().equals(_viewProcessId)) {
        return graphExecutionStatistics;
      }
    }
    return null;
  }

  @Override
  public Long getExecutedGraphs() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutedGraphs() : null; 
  }

  @Override
  public Long getExecutedNodes() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutedNodes() : null; 
  }

  @Override
  public Long getExecutionTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getExecutionTime() : null;  
  }

  @Override
  public Long getActualTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getActualTime() : 0;  
  }

  @Override
  public Long getProcessedJobs() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobs() : null; 
  }

  @Override
  public Long getProcessedJobSize() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobSize() : null;    
  }

  @Override
  public Long getProcessedJobCycleCost() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobCycleCost() : null;
  }

  @Override
  public Long getProcessedJobDataCost() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getProcessedJobDataCost() : null;
  }

  @Override
  public String getLastProcessedTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getLastProcessedTime().toString() : null;
  }

  @Override
  public String getLastExecutedTime() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    return graphExecutionStatistics != null ? graphExecutionStatistics.getLastExecutedTime().toString() : null;
  }

  @Override
  public void reset() {
    com.opengamma.engine.view.calc.stats.GraphExecutionStatistics graphExecutionStatistics = getGraphExecutionStatistics();
    if (graphExecutionStatistics != null) {
      graphExecutionStatistics.reset();
    }
    
  }
  
  /**
   * Gets the objectName field.
   * 
   * @return the object name for this MBean
   */
  public ObjectName getObjectName() {
    return _objectName;
  }

}
