/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of portfolios and positions/trades as accessed by the engine.
 * <p>
 * This interface provides a simple view of portfolios and positions as needed by the engine.
 * This may be backed by a full-featured position master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface PositionSource extends ChangeProvider {

  /**
   * Gets a portfolio by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single portfolio at a single version-correction.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the portfolio, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Portfolio getPortfolio(UniqueId uniqueId);

  /**
   * Gets a portfolio by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify
   * a single portfolio at a single version-correction.
   * 
   * @param objectId  the object identifier, not null
   * @param versionCorrection  the version-correction, not null
   * @return the portfolio, null if not found
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws RuntimeException if an error occurs
   */
  Portfolio getPortfolio(ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets a node by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single node at a single version-correction.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the node, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  PortfolioNode getPortfolioNode(UniqueId uniqueId);

  /**
   * Gets a position by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single position at a single version-correction.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the position, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Position getPosition(UniqueId uniqueId);

  /**
   * Gets a trade by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single trade at a single version-correction.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the trade, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Trade getTrade(UniqueId uniqueId);

}
