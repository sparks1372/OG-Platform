/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;

/**
 * Position master worker to get the position.
 */
public class QueryPositionDbPositionMasterWorker extends DbPositionMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorker.class);
  /**
   * SQL select for position.
   */
  protected static final String SELECT =
      "SELECT " +
        "p.id AS position_id, " +
        "p.oid AS position_oid, " +
        "p.portfolio_oid AS portfolio_oid, " +
        "p.parent_node_oid AS parent_node_oid, " +
        "p.ver_from_instant AS ver_from_instant, " +
        "p.ver_to_instant AS ver_to_instant, " +
        "p.corr_from_instant AS corr_from_instant, " +
        "p.corr_to_instant AS corr_to_instant, " +
        "p.quantity AS pos_quantity, " +
        "ps.key_scheme AS pos_key_scheme, " +
        "ps.key_value AS pos_key_value, " +
        "t.id AS trade_id, " +
        "t.oid AS trade_oid, " +
        "t.quantity AS trade_quantity, " +
        "t.trade_date AS trade_date, " +
        "t.trade_time AS trade_time, " +
        "t.zone_offset AS zone_offset, " +
        "t.cparty_scheme AS cparty_scheme, " +
        "t.cparty_value AS cparty_value, " +
        "ts.key_scheme AS trade_key_scheme, " +
        "ts.key_value AS trade_key_value ";
    
  /**
   * SQL from for position.
   */
  protected static final String FROM =
      "FROM pos_position p " +
      "LEFT JOIN pos_position2idkey pi ON (pi.position_id = p.id) " +
      "LEFT JOIN pos_idkey ps ON (pi.idkey_id = ps.id) " +
      "LEFT JOIN pos_trade t ON (p.id = t.position_id) " +
      "LEFT JOIN pos_trade2idkey ti ON (t.id = ti.trade_id) " +
      "LEFT JOIN pos_idkey ts ON (ti.idkey_id = ts.id) ";
  
  /**
   * Creates an instance.
   */
  public QueryPositionDbPositionMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionDocument getPosition(final UniqueIdentifier uid) {
    if (uid.isVersioned()) {
      return getPositionById(uid);
    } else {
      return getPositionByLatest(uid);
    }
  }

  /**
   * Gets a position by searching for the latest version of an object identifier.
   * @param uid  the unique identifier
   * @return the position document, null if not found
   */
  protected PositionDocument getPositionByLatest(final UniqueIdentifier uid) {
    s_logger.debug("getPositionByLatest: {}", uid);
    final Instant now = Instant.now(getTimeSource());
    final PositionHistoryRequest request = new PositionHistoryRequest(uid, now, now);
    final PositionHistoryResult result = getMaster().historyPosition(request);
    if (result.getDocuments().size() != 1) {
      throw new DataNotFoundException("Position not found: " + uid);
    }
    return result.getFirstDocument();
  }

  /**
   * Gets a position by identifier.
   * @param uid  the unique identifier
   * @return the position document, null if not found
   */
  protected PositionDocument getPositionById(final UniqueIdentifier uid) {
    s_logger.debug("getPositionById {}", uid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("position_id", extractRowId(uid));
    final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
    NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PositionDocument> docs = namedJdbc.query(sqlGetPositionById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Position not found: " + uid);
    }
    return docs.get(0);
  }


  /**
   * Gets the SQL for getting a position by unique row identifier.
   * @return the SQL, not null
   */
  protected String sqlGetPositionById() {
    return SELECT + FROM + "WHERE p.id = :position_id ";
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionSearchResult searchPositions(PositionSearchRequest request) {
    s_logger.debug("searchPositions: {}", request);
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(request.getVersionAsOfInstant(), now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(request.getCorrectedToInstant(), now))
      .addValueNullIgnored("min_quantity", request.getMinQuantity())
      .addValueNullIgnored("max_quantity", request.getMaxQuantity());
    if (request.getPortfolioId() != null) {
      args.addValue("portfolio_oid", extractOid(request.getPortfolioId()));
    }
    if (request.getParentNodeId() != null) {
      args.addValue("parent_node_oid", extractOid(request.getParentNodeId()));
    }
    // TODO: security key
    final PositionSearchResult result = new PositionSearchResult();
    searchWithPaging(request.getPagingRequest(), sqlSearchPositions(request), args, new PositionDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to search for positions.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchPositions(final PositionSearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
                "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getPortfolioId() != null) {
      where += "AND portfolio_oid = :portfolio_oid ";
    }
    if (request.getParentNodeId() != null) {
      where += "AND parent_node_oid = :parent_node_oid ";
    }
    if (request.getMinQuantity() != null) {
      where += "AND quantity >= :min_quantity ";
    }
    if (request.getMaxQuantity() != null) {
      where += "AND quantity < :max_quantity ";
    }
    String selectFromWhereInner = "SELECT id FROM pos_position " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY id ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE p.id IN (" + inner + ") ORDER BY p.id, t.id ";
    String count = "SELECT COUNT(*) FROM pos_position " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionHistoryResult historyPosition(final PositionHistoryRequest request) {
    s_logger.debug("searchPositionHistoric: {}", request);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("position_oid", extractOid(request.getObjectId()))
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addTimestampNullIgnored("corrections_from_instant", request.getCorrectionsFromInstant())
      .addTimestampNullIgnored("corrections_to_instant", request.getCorrectionsToInstant());
    final PositionHistoryResult result = new PositionHistoryResult();
    searchWithPaging(request.getPagingRequest(), sqlSearchPositionHistoric(request), args, new PositionDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL for searching the history of a position.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchPositionHistoric(final PositionHistoryRequest request) {
    String where = "WHERE oid = :position_oid ";
    if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
      where += "AND ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant ";
    } else {
      if (request.getVersionsFromInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant) " +
                            "OR ver_from_instant >= :versions_from_instant) ";
      }
      if (request.getVersionsToInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_to_instant AND ver_to_instant > :versions_to_instant) " +
                            "OR ver_to_instant < :versions_to_instant) ";
      }
    }
    if (request.getCorrectionsFromInstant() != null && request.getCorrectionsFromInstant().equals(request.getCorrectionsToInstant())) {
      where += "AND corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant ";
    } else {
      if (request.getCorrectionsFromInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant) " +
                            "OR corr_from_instant >= :corrections_from_instant) ";
      }
      if (request.getCorrectionsToInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_to_instant AND ver_to_instant > :corrections_to_instant) " +
                            "OR corr_to_instant < :corrections_to_instant) ";
      }
    }
    String selectFromWhereInner = "SELECT id FROM pos_position " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE p.id IN (" + inner + ") ORDER BY p.ver_from_instant DESC, p.corr_from_instant DESC, t.id DESC";
    String count = "SELECT COUNT(*) FROM pos_position " + where;
    return new String[] {search, count};
  }

  /**
   * Searches for documents with paging.
   * 
   * @param pagingRequest  the paging request, not null
   * @param sql  the array of SQL, query and count, not null
   * @param args  the query arguments, not null
   * @param extractor  the extractor of results, not null
   * @param result  the object to populate, not null
   */
  protected void searchWithPaging(
      final PagingRequest pagingRequest, final String[] sql, final DbMapSqlParameterSource args,
      final ResultSetExtractor<List<PositionDocument>> extractor, final AbstractDocumentsResult<PositionDocument> result) {
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    if (pagingRequest.equals(PagingRequest.ALL)) {
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      result.setPaging(Paging.of(result.getDocuments(), pagingRequest));
    } else {
      final int count = namedJdbc.queryForInt(sql[1], args);
      result.setPaging(new Paging(pagingRequest, count));
      if (count > 0) {
        result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PositionDocument.
   */
  protected final class PositionDocumentExtractor implements ResultSetExtractor<List<PositionDocument>> {
    private long _lastPositionId = -1;
    private long _lastTradeId = -1;
    private ManageablePosition _position;
    private List<PositionDocument> _documents = new ArrayList<PositionDocument>();
    private Map<UniqueIdentifier, UniqueIdentifier> _deduplicate = Maps.newHashMap();

    @Override
    public List<PositionDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      ManageableTrade currentTrade = null;
      while (rs.next()) {
        
        final long positionId = rs.getLong("POSITION_ID");
        if (_lastPositionId != positionId) {
          _lastPositionId = positionId;
          buildPosition(rs, positionId);
        }
        
        final String posIdScheme = rs.getString("POS_KEY_SCHEME");
        final String posIdValue = rs.getString("POS_KEY_VALUE");
        if (posIdScheme != null && posIdValue != null) {
          Identifier id = Identifier.of(posIdScheme, posIdValue);
          _position.setSecurityKey(_position.getSecurityKey().withIdentifier(id));
        }
        
        final long tradeId = rs.getLong("TRADE_ID");
        if (_lastTradeId != tradeId && tradeId != 0) {
          _lastTradeId = tradeId;
          final BigDecimal tradeQuantity = extractBigDecimal(rs, "TRADE_QUANTITY");
          LocalDate tradeDate = DbDateUtils.fromSqlDate(rs.getDate("TRADE_DATE"));
          LocalTime tradeTime = DbDateUtils.fromSqlTime(rs.getTimestamp("TRADE_TIME"));
          int zoneOffset = rs.getInt("ZONE_OFFSET");
          OffsetTime tradeOffsetTime = null;
          if (tradeTime != null) {
            tradeOffsetTime = OffsetTime.of(tradeTime, ZoneOffset.ofTotalSeconds(zoneOffset));
          }
          final String cpartyScheme = rs.getString("CPARTY_SCHEME");
          final String cpartyValue = rs.getString("CPARTY_VALUE");
          Identifier counterpartyId = null;
          if (cpartyScheme != null && cpartyValue != null) {
            counterpartyId = Identifier.of(cpartyScheme, cpartyValue);
          }
          currentTrade = new ManageableTrade(tradeQuantity, tradeDate, tradeOffsetTime, counterpartyId, IdentifierBundle.EMPTY);
          long tradeOid = rs.getLong("TRADE_OID");
          currentTrade.setUniqueIdentifier(createUniqueIdentifier(tradeOid, tradeId, _deduplicate));
          currentTrade.setPositionId(_position.getUniqueIdentifier());
          _position.getTrades().add(currentTrade);
        }
        
        final String tradeIdScheme = rs.getString("TRADE_KEY_SCHEME");
        final String tradeIdValue = rs.getString("TRADE_KEY_VALUE");
        if (tradeIdScheme != null && tradeIdValue != null) {
          Identifier id = Identifier.of(tradeIdScheme, tradeIdValue);
          currentTrade.setSecurityKey(currentTrade.getSecurityKey().withIdentifier(id));
        }

      }
      return _documents;
    }

    private void buildPosition(final ResultSet rs, final long positionId) throws SQLException {
      final long positionOid = rs.getLong("POSITION_OID");
      final long portfolioOid = rs.getLong("PORTFOLIO_OID");
      final long parentNodeOid = rs.getLong("PARENT_NODE_OID");
      final BigDecimal quantity = extractBigDecimal(rs, "POS_QUANTITY");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      _position = new ManageablePosition(quantity, IdentifierBundle.EMPTY);
      _position.setUniqueIdentifier(createUniqueIdentifier(positionOid, positionId, _deduplicate));
      PositionDocument doc = new PositionDocument(_position);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setPortfolioId(createObjectIdentifier(portfolioOid, _deduplicate));
      doc.setParentNodeId(createObjectIdentifier(parentNodeOid, _deduplicate));
      doc.setUniqueId(createUniqueIdentifier(positionOid, positionId, _deduplicate));
      _documents.add(doc);
    }
  }
  
  /**
   * Mapper from SQL rows to a ManageableTrade.
   */
  protected final class ManageableTradeExtractor implements ResultSetExtractor<List<ManageableTrade>> {
    private List<ManageableTrade> _tradeList = new ArrayList<ManageableTrade>();
    private long _lastTradeId = -1;
    private ManageableTrade _trade;
    private Map<UniqueIdentifier, UniqueIdentifier> _duplicate = Maps.newHashMap();
    private final UniqueIdentifier _positionId;
    
    /**
     * @param positionId the position unique identifier, not -null
     */
    public ManageableTradeExtractor(UniqueIdentifier positionId) {
      ArgumentChecker.notNull(positionId, "position id");
      _positionId = positionId;
    }

    @Override
    public List<ManageableTrade> extractData(ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long tradeId = rs.getLong("TRADE_ID");
        if (_lastTradeId != tradeId) {
          _lastTradeId = tradeId;
          buildTrade(rs, tradeId);
        }
        final String idScheme = rs.getString("SECKEY_SCHEME");
        final String idValue = rs.getString("SECKEY_VALUE");
        if (idScheme != null && idValue != null) {
          Identifier id = Identifier.of(idScheme, idValue);
          _trade.setSecurityKey(_trade.getSecurityKey().withIdentifier(id));
        }
      }
      return _tradeList;
    }
    
    private void buildTrade(final ResultSet rs, final long tradeId) throws SQLException {
      final long tradeOid = rs.getLong("TRADE_OID");
      final UniqueIdentifier tradeUid = createUniqueIdentifier(tradeOid, tradeId, _duplicate);
      
      final BigDecimal quantity = extractBigDecimal(rs, "TRADE_QUANTITY");
      final Date tradeDate = rs.getDate("TRADE_DATE");
      final Timestamp tradeTime = rs.getTimestamp("TRADE_TIME");
      int timeZoneOffInSec = rs.getInt("ZONE_OFFSET");
      OffsetTime tradeOffsetTime = null;
      if (tradeTime != null) {
        tradeOffsetTime = OffsetTime.of(DbDateUtils.fromSqlTime(tradeTime), ZoneOffset.ofTotalSeconds(timeZoneOffInSec));
      }
      
      final String cpartyScheme = rs.getString("CPARTY_SCHEME");
      final String cpartyValue = rs.getString("CPARTY_VALUE");
      Identifier counterpartyId = Identifier.of(cpartyScheme, cpartyValue);
      
      _trade = new ManageableTrade();
      _trade.setQuantity(quantity);
      _trade.setTradeDate(DbDateUtils.fromSqlDate(tradeDate));
      _trade.setTradeTime(tradeOffsetTime);
      _trade.setCounterpartyId(counterpartyId);
      _trade.setUniqueIdentifier(tradeUid);
      _trade.setPositionId(_positionId);
      _trade.setSecurityKey(IdentifierBundle.EMPTY);
      _tradeList.add(_trade);
    }
  }

}