/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class FixedDistributionSpecificationResolver implements DistributionSpecificationResolver {
  
  private final Map<LiveDataSpecification, DistributionSpecification> _liveDataSpec2DistSpec;
  
  public FixedDistributionSpecificationResolver(Map<LiveDataSpecification, DistributionSpecification> fixes) {
    ArgumentChecker.notNull(fixes, "Fixed distribution specifications");
    _liveDataSpec2DistSpec = new HashMap<LiveDataSpecification, DistributionSpecification>(fixes);
  }

  @Override
  public DistributionSpecification getDistributionSpecification(LiveDataSpecification liveDataSpecificationFromClient) throws IllegalArgumentException {
    DistributionSpecification spec = _liveDataSpec2DistSpec.get(liveDataSpecificationFromClient);
    if (spec == null) {
      throw new IllegalArgumentException("No distribution specification found for " + liveDataSpecificationFromClient);
    }
    return spec;
  }
  
}