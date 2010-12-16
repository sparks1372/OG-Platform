/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.tuple.Pair;

/**
 * Position master worker to modify a position.
 */
public class ModifyPositionDbPositionMasterWorker extends DbPositionMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorker.class);

  /**
   * Creates an instance.
   */
  public ModifyPositionDbPositionMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionDocument addPosition(final PositionDocument document) {
    s_logger.debug("addPosition {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // insert new row
        final Instant now = Instant.now(getTimeSource());
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setParentNodeId(document.getParentNodeId().toLatest());
        document.setPortfolioId(checkNodeGetPortfolioOid(document));
        document.setUniqueId(null);
        insertPosition(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionDocument updatePosition(final PositionDocument document) {
    final UniqueIdentifier uid = document.getUniqueId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("updatePosition {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final PositionDocument oldDoc = getPositionCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setParentNodeId(oldDoc.getParentNodeId());
        document.setPortfolioId(oldDoc.getPortfolioId());
        document.setUniqueId(oldDoc.getUniqueId().toLatest());
        insertPosition(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void removePosition(final UniqueIdentifier uid) {
    s_logger.debug("removePosition {}", uid);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final PositionDocument oldDoc = getPositionCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionDocument correctPosition(final PositionDocument document) {
    final UniqueIdentifier uid = document.getUniqueId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("correctPosition {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final PositionDocument oldDoc = getPositionCheckLatestCorrection(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setCorrectionToInstant(now);
        updateCorrectionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(oldDoc.getVersionFromInstant());
        document.setVersionToInstant(oldDoc.getVersionToInstant());
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setParentNodeId(oldDoc.getParentNodeId());
        document.setPortfolioId(oldDoc.getPortfolioId());
        document.setUniqueId(oldDoc.getUniqueId().toLatest());
        insertPosition(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  /**
   * Validates that the node exists, returning the associated portfolio oid.
   * @param document  the document, not null
   * @return the portfolio oid, not null
   */
  protected UniqueIdentifier checkNodeGetPortfolioOid(final PositionDocument document) {
    final UniqueIdentifier nodeOid = document.getParentNodeId().toLatest();
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("node_oid", extractOid(nodeOid))
      .addTimestamp("version_as_of_instant", document.getVersionFromInstant())
      .addTimestamp("corrected_to_instant", document.getCorrectionFromInstant());
    final List<Map<String, Object>> data = getJdbcTemplate().queryForList(sqlSelectCheckNodeGetPortfolioOid(), args);
    if (data.size() == 0) {
      throw new DataNotFoundException("Parent node not found: " + nodeOid);
    }
    if (data.size() > 1 || data.get(0).containsKey("PORTFOLIO_OID") == false) {
      throw new IncorrectResultSizeDataAccessException("Parent node table invalid: " + nodeOid, 1, data.size());
    }
    final long portfolioOid = (Long) data.get(0).get("PORTFOLIO_OID");
    return createObjectIdentifier(portfolioOid, null);
  }

  /**
   * Gets the SQL that checks if the portfolio and node exists.
   * @return the SQL, not null
   */
  protected String sqlSelectCheckNodeGetPortfolioOid() {
    return "SELECT DISTINCT f.oid AS portfolio_oid " +
            "FROM pos_node n, pos_portfolio f " +
            "WHERE n.portfolio_id = f.id " +
              "AND n.oid = :node_oid " +
              "AND (f.ver_from_instant <= :version_as_of_instant AND f.ver_to_instant > :version_as_of_instant) " +
              "AND (f.corr_from_instant <= :corrected_to_instant AND f.corr_to_instant > :corrected_to_instant) ";
  }
  
  /**
   * Gets the SQL for selecting an idkey.
   * @return the SQL, not null
   */
  protected String sqlSelectIdKey() {
    return "SELECT id FROM pos_idkey WHERE key_scheme = :key_scheme AND key_value = :key_value";
  }
  
  /**
   * Gets the SQL for inserting an idkey.
   * @return the SQL, not null
   */
  protected String sqlInsertIdKey() {
    return "INSERT INTO pos_idkey (id, key_scheme, key_value) " +
            "VALUES (:idkey_id, :key_scheme, :key_value)";
  }
  
  /**
   * Gets the SQL for inserting a position-idkey association.
   * @return the SQL, not null
   */
  protected String sqlInsertPositionIdKey() {
    return "INSERT INTO pos_position2idkey " +
              "(position_id, idkey_id) " +
            "VALUES " +
              "(:position_id, (" + sqlSelectIdKey() + "))";
  }
  
  /**
   * Gets the SQL for inserting a position-idkey association.
   * @return the SQL, not null
   */
  protected String sqlInsertTradeIdKey() {
    return "INSERT INTO pos_trade2idkey " +
              "(trade_id, idkey_id) " +
            "VALUES " +
              "(:trade_id, (" + sqlSelectIdKey() + "))";
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a position.
   * @param document  the document, not null
   */
  protected void insertPosition(final PositionDocument document) {
    final long positionId = nextId();
    final long positionOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : positionId);
    // the arguments for inserting into the position table
    final DbMapSqlParameterSource positionArgs = new DbMapSqlParameterSource()
      .addValue("position_id", positionId)
      .addValue("position_oid", positionOid)
      .addValue("portfolio_oid", extractOid(document.getPortfolioId()))
      .addValue("parent_node_oid", extractOid(document.getParentNodeId()))
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("quantity", document.getPosition().getQuantity());
    
    // the arguments for inserting into the idkey tables
    final List<DbMapSqlParameterSource> posAssocList = new ArrayList<DbMapSqlParameterSource>();
    final Set<Pair<String, String>> schemeValueSet = Sets.newHashSet();
    
    for (Identifier id : document.getPosition().getSecurityKey()) {
      final DbMapSqlParameterSource assocArgs = new DbMapSqlParameterSource()
        .addValue("position_id", positionId)
        .addValue("key_scheme", id.getScheme().getName())
        .addValue("key_value", id.getValue());
      posAssocList.add(assocArgs);
      schemeValueSet.add(Pair.of(id.getScheme().getName(), id.getValue()));
    }
    
    final UniqueIdentifier positionUid = createUniqueIdentifier(positionOid, positionId, null);
    //the arguments for inserting into the trade table
    final List<DbMapSqlParameterSource> tradeList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> tradeAssocList = new ArrayList<DbMapSqlParameterSource>();
    
    for (ManageableTrade trade : document.getPosition().getTrades()) {
      final long tradeId = nextId();
      final long tradeOid = (trade.getUniqueIdentifier() != null ? extractOid(trade.getUniqueIdentifier()) : tradeId);
      final Identifier counterpartyId = trade.getCounterpartyId();
      final DbMapSqlParameterSource tradeArgs = new DbMapSqlParameterSource()
        .addValue("trade_id", tradeId)
        .addValue("trade_oid", tradeOid)
        .addValue("position_id", positionId)
        .addValue("position_oid", positionOid)
        .addValue("quantity", trade.getQuantity())
        .addDate("trade_date", trade.getTradeDate())
        .addTime("trade_time", trade.getTradeTime().toLocalTime())
        .addValue("zone_offset", trade.getTradeTime().getOffset().getAmountSeconds())
        .addValue("cparty_scheme", counterpartyId.getScheme().getName())
        .addValue("cparty_value", counterpartyId.getValue());
      tradeList.add(tradeArgs);
      //set the trade uid
      final UniqueIdentifier tradeUid = createUniqueIdentifier(tradeOid, tradeId, null);
      UniqueIdentifiables.setInto(trade, tradeUid);
      trade.setPositionId(positionUid);
      
      for (Identifier id : trade.getSecurityKey()) {
        final DbMapSqlParameterSource assocArgs = new DbMapSqlParameterSource()
          .addValue("trade_id", tradeId)
          .addValue("key_scheme", id.getScheme().getName())
          .addValue("key_value", id.getValue());
        tradeAssocList.add(assocArgs);
        schemeValueSet.add(Pair.of(id.getScheme().getName(), id.getValue()));
      }
    }
    
    final List<DbMapSqlParameterSource> idKeyList = new ArrayList<DbMapSqlParameterSource>();
    for (Pair<String, String> pair : schemeValueSet) {
      final DbMapSqlParameterSource idkeyArgs = new DbMapSqlParameterSource()
        .addValue("key_scheme", pair.getFirst())
        .addValue("key_value", pair.getSecond());
      if (getJdbcTemplate().queryForList(sqlSelectIdKey(), idkeyArgs).isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long idKeyId = nextId("pos_idkey_seq");
        idkeyArgs.addValue("idkey_id", idKeyId);
        idKeyList.add(idkeyArgs);
      }
    }
    
    getJdbcTemplate().update(sqlInsertPosition(), positionArgs);
    getJdbcTemplate().batchUpdate(sqlInsertIdKey(), idKeyList.toArray(new DbMapSqlParameterSource[idKeyList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertPositionIdKey(), posAssocList.toArray(new DbMapSqlParameterSource[posAssocList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertTrades(), tradeList.toArray(new DbMapSqlParameterSource[tradeList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertTradeIdKey(), tradeAssocList.toArray(new DbMapSqlParameterSource[tradeAssocList.size()]));
    
    // set the uid
    UniqueIdentifiables.setInto(document.getPosition(), positionUid);
    document.setUniqueId(positionUid);
    
  }

  /**
   * Gets the SQL for inserting a trade.
   * @return the SQL, not null
   */
  protected String sqlInsertTrades() {
    return "INSERT INTO pos_trade " +
              "(id, oid, position_id, position_oid, quantity, trade_date, trade_time, zone_offset, cparty_scheme, cparty_value) " +
            "VALUES " +
              "(:trade_id, :trade_oid, :position_id, :position_oid, :quantity, :trade_date, :trade_time, :zone_offset, :cparty_scheme, :cparty_value)";
  }

  /**
   * Gets the SQL for inserting a position.
   * @return the SQL, not null
   */
  protected String sqlInsertPosition() {
    return "INSERT INTO pos_position " +
              "(id, oid, portfolio_oid, parent_node_oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, quantity) " +
            "VALUES " +
              "(:position_id, :position_oid, :portfolio_oid, :parent_node_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :quantity)";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected PositionDocument getPositionCheckLatestVersion(final UniqueIdentifier uid) {
    final PositionDocument oldDoc = getMaster().getPosition(uid);  // checks uid exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the version as ended.
   * @param document  the document to update, not null
   */
  protected void updateVersionToInstant(final PositionDocument document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("position_id", extractRowId(document.getUniqueId()))
      .addTimestamp("ver_to_instant", document.getVersionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateVersionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end version instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end version of a position.
   * @return the SQL, not null
   */
  protected String sqlUpdateVersionToInstant() {
    return "UPDATE pos_position " +
              "SET ver_to_instant = :ver_to_instant " +
            "WHERE id = :position_id " +
              "AND ver_to_instant = :max_instant ";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected PositionDocument getPositionCheckLatestCorrection(final UniqueIdentifier uid) {
    final PositionDocument oldDoc = getMaster().getPosition(uid);  // checks uid exists
    if (oldDoc.getCorrectionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest correction: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the correction as ended.
   * @param document  the document to update, not null
   */
  protected void updateCorrectionToInstant(final PositionDocument document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("position_id", extractRowId(document.getUniqueId()))
      .addTimestamp("corr_to_instant", document.getCorrectionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateCorrectionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end correction instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end correction of a position.
   * @return the SQL, not null
   */
  protected String sqlUpdateCorrectionToInstant() {
    return "UPDATE pos_position " +
              "SET corr_to_instant = :corr_to_instant " +
            "WHERE id = :position_id " +
              "AND corr_to_instant = :max_instant ";
  }

}