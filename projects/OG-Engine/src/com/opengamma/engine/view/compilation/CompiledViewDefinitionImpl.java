/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link CompiledViewDefinition}.
 */
public class CompiledViewDefinitionImpl implements CompiledViewDefinition {

  private final ViewDefinition _viewDefinition;
  private final Portfolio _portfolio;
  private final Map<String, CompiledViewCalculationConfiguration> _compiledCalculationConfigurations;
  private final Instant _earliestValidity;
  private final Instant _latestValidity;

  public CompiledViewDefinitionImpl(ViewDefinition viewDefinition, Portfolio portfolio,
      Collection<CompiledViewCalculationConfiguration> compiledCalculationConfigurations,
      Instant earliestValidity, Instant latestValidity) {
    _viewDefinition = viewDefinition;
    _portfolio = portfolio;
    _compiledCalculationConfigurations = new HashMap<String, CompiledViewCalculationConfiguration>();
    for (CompiledViewCalculationConfiguration compiledCalculationConfiguration : compiledCalculationConfigurations) {
      _compiledCalculationConfigurations.put(compiledCalculationConfiguration.getName(), compiledCalculationConfiguration);
    }
    _earliestValidity = earliestValidity;
    _latestValidity = latestValidity;
  }
  
  @Override
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  @Override
  public Portfolio getPortfolio() {
    return _portfolio;
  }
  
  @Override
  public CompiledViewCalculationConfiguration getCompiledCalculationConfiguration(String viewCalculationConfiguration) {
    ArgumentChecker.notNull(viewCalculationConfiguration, "viewCalculationConfiguration");
    return _compiledCalculationConfigurations.get(viewCalculationConfiguration);
  }
  
  @Override
  public Collection<CompiledViewCalculationConfiguration> getCompiledCalculationConfigurations() {
    return Collections.unmodifiableCollection(_compiledCalculationConfigurations.values());
  }

  @Override
  public Map<ValueRequirement, ValueSpecification> getMarketDataRequirements() {
    Map<ValueRequirement, ValueSpecification> allRequirements = new HashMap<ValueRequirement, ValueSpecification>();
    for (CompiledViewCalculationConfiguration compiledCalcConfig : getCompiledCalculationConfigurations()) {
      allRequirements.putAll(compiledCalcConfig.getMarketDataRequirements());
    }
    return Collections.unmodifiableMap(allRequirements);
  }

  @Override
  public Set<ComputationTarget> getComputationTargets() {
    Set<ComputationTarget> allTargets = new HashSet<ComputationTarget>();
    for (CompiledViewCalculationConfiguration compiledCalcConfig : getCompiledCalculationConfigurations()) {
      allTargets.addAll(compiledCalcConfig.getComputationTargets());
    }
    return Collections.unmodifiableSet(allTargets);
  }

  @Override
  public Instant getValidFrom() {
    return _earliestValidity;
  }

  @Override
  public Instant getValidTo() {
    return _latestValidity;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Checks whether the compilation results encapsulated in this instance are valid for a specific cycle. Note that
   * this does not ensure that the view definition used for compilation is still up-to-date.
   * 
   * @param valuationTimeProvider  the valuation time, not {@code null}
   * @return  {@code true} if the compilation results are valid for the valuation time, {@code false} otherwise
   */
  public boolean isValidFor(final InstantProvider valuationTimeProvider) {
    ArgumentChecker.notNull(valuationTimeProvider, "valuationTimeProvider");
    Instant valuationTime = valuationTimeProvider.toInstant();
    return (_earliestValidity == null || !valuationTime.isBefore(_earliestValidity))
        && (_latestValidity == null || !valuationTime.isAfter(_latestValidity));
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "CompiledViewDefinition[" + getViewDefinition().getName() + ", " + getValidityString() + "]";
  }
  
  protected String getValidityString() {
    if (_earliestValidity == null && _latestValidity == null) {
      return "unrestricted validity";
    } else if (_earliestValidity == null) {
      return "valid until " + _latestValidity.toString();
    } else if (_latestValidity == null) {
      return "valid from " + _earliestValidity.toString();
    } else {
      return "valid from " + _earliestValidity.toString() + " to " + _latestValidity.toString();
    }
  }

}
