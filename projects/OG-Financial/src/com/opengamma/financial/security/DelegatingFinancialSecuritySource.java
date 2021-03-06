/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of securities that uses the scheme of the unique identifier to determine which
 * underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingFinancialSecuritySource
    extends UniqueIdSchemeDelegator<FinancialSecuritySource>
    implements FinancialSecuritySource {

  /**
   * The change manager
   */
  private final ChangeManager _changeManager;
  
  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   */
  public DelegatingFinancialSecuritySource(FinancialSecuritySource defaultSource) {
    super(defaultSource);
    _changeManager = defaultSource.changeManager();
  }

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   * @param schemePrefixToSourceMap  the map of sources by scheme to switch on, not null
   */
  public DelegatingFinancialSecuritySource(FinancialSecuritySource defaultSource, Map<String, FinancialSecuritySource> schemePrefixToSourceMap) {
    super(defaultSource, schemePrefixToSourceMap);
    
    AggregatingChangeManager changeManager = new AggregatingChangeManager();
    changeManager.addChangeManager(defaultSource.changeManager());
    for (FinancialSecuritySource source : schemePrefixToSourceMap.values()) {
      changeManager.addChangeManager(source.changeManager());
    }
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueId uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid.getScheme()).getSecurity(uid);
  }
  
  @Override
  public Security getSecurity(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return chooseDelegate(objectId.getScheme()).getSecurity(objectId, versionCorrection);
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : getDelegates().values()) {
      Collection<Security> result = delegateSource.getSecurities(bundle);
      if (!result.isEmpty()) {
        return result;
      }
    }
    return getDefaultDelegate().getSecurities(bundle);
  }
  
  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : getDelegates().values()) {
      Collection<Security> result = delegateSource.getSecurities(bundle, versionCorrection);
      if (!result.isEmpty()) {
        return result;
      }
    }
    return getDefaultDelegate().getSecurities(bundle, versionCorrection);
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : getDelegates().values()) {
      Security result = delegateSource.getSecurity(bundle);
      if (result != null) {
        return result;
      }
    }
    return getDefaultDelegate().getSecurity(bundle);
  }
  
  @Override
  public Security getSecurity(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(bundle, "bundle");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(bundle, "bundle");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : getDelegates().values()) {
      Security result = delegateSource.getSecurity(bundle, versionCorrection);
      if (result != null) {
        return result;
      }
    }
    return getDefaultDelegate().getSecurity(bundle, versionCorrection);
  }

  @Override
  public Collection<Security> getBondsWithIssuerName(String issuerName) {
    // best implementation is to return first matching result
    for (FinancialSecuritySource delegateSource : getDelegates().values()) {
      Collection<Security> result = delegateSource.getBondsWithIssuerName(issuerName);
      if (!result.isEmpty()) {
        return result;
      }
    }
    return getDefaultDelegate().getBondsWithIssuerName(issuerName);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
