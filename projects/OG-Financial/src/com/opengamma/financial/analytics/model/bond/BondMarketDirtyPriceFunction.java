/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 */
public class BondMarketDirtyPriceFunction extends BondFunction {

  public BondMarketDirtyPriceFunction() {
    _requirementName = MarketDataRequirementNames.DIRTY_PRICE_MID;
  }
  
  @Override
  protected Set<ComputedValue> getComputedValues(Position position, Bond bond, double value) {
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.MARKET_DIRTY_PRICE, position), getUniqueIdentifier());
    return Sets.newHashSet(new ComputedValue(specification, value));
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.MARKET_DIRTY_PRICE, target.getPosition()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "BondMarketDirtyPriceFunction";
  }

}