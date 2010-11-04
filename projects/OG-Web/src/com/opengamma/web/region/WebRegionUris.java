/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.region;

import java.net.URI;

import com.opengamma.financial.Currency;
import com.opengamma.financial.world.region.Region;
import com.opengamma.financial.world.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * URIs for web-based regions.
 */
public class WebRegionUris {

  /**
   * The data.
   */
  private final WebRegionData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebRegionUris(WebRegionData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI regions() {
    return WebRegionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param identifier  the identifier to search for, may be null
   * @return the URI
   */
  public URI regions(final Identifier identifier) {
    return WebRegionsResource.uri(_data, IdentifierBundle.of(identifier));
  }

  /**
   * Gets the URI.
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI
   */
  public URI regions(final IdentifierBundle identifiers) {
    return WebRegionsResource.uri(_data, identifiers);
  }

  /**
   * Gets the URI.
   * @param currencyISO  the currency to search for, may be null
   * @return the URI
   */
  public URI regionsByCurrency(final String currencyISO) {
    try {
      return WebRegionsResource.uri(_data, IdentifierBundle.of(RegionUtils.currencyRegionId(Currency.getInstance(currencyISO))));
    } catch (Exception ex) {
      return WebRegionResource.uri(_data);
    }
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI region() {
    return WebRegionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param region  the region, not null
   * @return the URI
   */
  public URI region(final Region region) {
    return WebRegionResource.uri(_data, region.getUniqueIdentifier());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI regionVersions() {
    return WebRegionVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI regionVersion() {
    return WebRegionVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param region  the region, not null
   * @return the URI
   */
  public URI regionVersion(final Region region) {
    return WebRegionVersionResource.uri(_data, region.getUniqueIdentifier());
  }

}