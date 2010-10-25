/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * Request for searching for historic holiday information.
 * <p>
 * A full holiday master implements historical storage of data.
 * History can be stored in two dimensions and this request provides searching.
 * <p>
 * The first historic dimension is the classic series of versions.
 * Each new version is stored in such a manor that previous versions can be accessed.
 * <p>
 * The second historic dimension is corrections.
 * A correction occurs when it is realized that the original data stored was incorrect.
 * A simple holiday master might simply replace the original version with the corrected value.
 * A full implementation will store the correction in such a manner that it is still possible
 * to obtain the value before the correction was made.
 * <p>
 * For example, an holiday added on Monday and updated on Thursday has two versions.
 * If it is realized on Friday that the version stored on Monday was incorrect, then a
 * correction may be applied. There are now two versions, the first of which has one correction.
 * This may continue, with multiple corrections allowed for each version.
 * <p>
 * Versions are represented by instants in the search.
 */
@BeanDefinition
public class HolidaySearchHistoricRequest extends DirectBean {

  /**
   * The request for paging.
   * By default all matching items will be returned.
   */
  @PropertyDefinition
  private PagingRequest _pagingRequest = PagingRequest.ALL;
  /**
   * The holiday object identifier to match.
   */
  @PropertyDefinition
  private UniqueIdentifier _holidayId;
  /**
   * The instant to retrieve versions on or after (inclusive).
   * If this instant equals the {@code versionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version.
   */
  @PropertyDefinition
  private Instant _versionsFromInstant;
  /**
   * The instant to retrieve versions before (exclusive).
   * If this instant equals the {@code versionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest version.
   * This should be equal to or later than the {@code versionsFromInstant}.
   */
  @PropertyDefinition
  private Instant _versionsToInstant;
  /**
   * The instant to retrieve corrections on or after (inclusive).
   * If this instant equals the {@code correctionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version prior to corrections.
   * This should be equal to or later than the {@code versionsFromInstant}.
   */
  @PropertyDefinition
  private Instant _correctionsFromInstant;
  /**
   * The instant to retrieve corrections before (exclusive).
   * If this instant equals the {@code correctionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest correction.
   * This should be equal to or later than the {@code correctionsFromInstant}.
   */
  @PropertyDefinition
  private Instant _correctionsToInstant;

  /**
   * Creates an instance.
   * The object identifier must be added before searching.
   */
  public HolidaySearchHistoricRequest() {
  }

  /**
   * Creates an instance.
   * With no further customization this will retrieve all versions and corrections.
   * @param oid  the object identifier
   */
  public HolidaySearchHistoricRequest(final UniqueIdentifier oid) {
    this(oid, null, null);
  }

  /**
   * Creates an instance.
   * @param oid  the object identifier
   * @param versionInstantProvider  the version instant to retrieve, null for all versions
   * @param correctedToInstantProvider  the instant that the data should be corrected to, null for all corrections
   */
  public HolidaySearchHistoricRequest(final UniqueIdentifier oid, InstantProvider versionInstantProvider, InstantProvider correctedToInstantProvider) {
    setHolidayId(oid);
    if (versionInstantProvider != null) {
      final Instant versionInstant = Instant.of(versionInstantProvider);
      setVersionsFromInstant(versionInstant);
      setVersionsToInstant(versionInstant);
    }
    if (correctedToInstantProvider != null) {
      final Instant correctedToInstant = Instant.of(correctedToInstantProvider);
      setCorrectionsFromInstant(correctedToInstant);
      setCorrectionsToInstant(correctedToInstant);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HolidaySearchHistoricRequest}.
   * @return the meta-bean, not null
   */
  public static HolidaySearchHistoricRequest.Meta meta() {
    return HolidaySearchHistoricRequest.Meta.INSTANCE;
  }

  @Override
  public HolidaySearchHistoricRequest.Meta metaBean() {
    return HolidaySearchHistoricRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -2092032669:  // pagingRequest
        return getPagingRequest();
      case 1349286803:  // holidayId
        return getHolidayId();
      case 825630012:  // versionsFromInstant
        return getVersionsFromInstant();
      case 288644747:  // versionsToInstant
        return getVersionsToInstant();
      case -1002076478:  // correctionsFromInstant
        return getCorrectionsFromInstant();
      case -1241747055:  // correctionsToInstant
        return getCorrectionsToInstant();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -2092032669:  // pagingRequest
        setPagingRequest((PagingRequest) newValue);
        return;
      case 1349286803:  // holidayId
        setHolidayId((UniqueIdentifier) newValue);
        return;
      case 825630012:  // versionsFromInstant
        setVersionsFromInstant((Instant) newValue);
        return;
      case 288644747:  // versionsToInstant
        setVersionsToInstant((Instant) newValue);
        return;
      case -1002076478:  // correctionsFromInstant
        setCorrectionsFromInstant((Instant) newValue);
        return;
      case -1241747055:  // correctionsToInstant
        setCorrectionsToInstant((Instant) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the request for paging.
   * By default all matching items will be returned.
   * @return the value of the property
   */
  public PagingRequest getPagingRequest() {
    return _pagingRequest;
  }

  /**
   * Sets the request for paging.
   * By default all matching items will be returned.
   * @param pagingRequest  the new value of the property
   */
  public void setPagingRequest(PagingRequest pagingRequest) {
    this._pagingRequest = pagingRequest;
  }

  /**
   * Gets the the {@code pagingRequest} property.
   * By default all matching items will be returned.
   * @return the property, not null
   */
  public final Property<PagingRequest> pagingRequest() {
    return metaBean().pagingRequest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday object identifier to match.
   * @return the value of the property
   */
  public UniqueIdentifier getHolidayId() {
    return _holidayId;
  }

  /**
   * Sets the holiday object identifier to match.
   * @param holidayId  the new value of the property
   */
  public void setHolidayId(UniqueIdentifier holidayId) {
    this._holidayId = holidayId;
  }

  /**
   * Gets the the {@code holidayId} property.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> holidayId() {
    return metaBean().holidayId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant to retrieve versions on or after (inclusive).
   * If this instant equals the {@code versionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version.
   * @return the value of the property
   */
  public Instant getVersionsFromInstant() {
    return _versionsFromInstant;
  }

  /**
   * Sets the instant to retrieve versions on or after (inclusive).
   * If this instant equals the {@code versionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version.
   * @param versionsFromInstant  the new value of the property
   */
  public void setVersionsFromInstant(Instant versionsFromInstant) {
    this._versionsFromInstant = versionsFromInstant;
  }

  /**
   * Gets the the {@code versionsFromInstant} property.
   * If this instant equals the {@code versionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version.
   * @return the property, not null
   */
  public final Property<Instant> versionsFromInstant() {
    return metaBean().versionsFromInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant to retrieve versions before (exclusive).
   * If this instant equals the {@code versionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest version.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @return the value of the property
   */
  public Instant getVersionsToInstant() {
    return _versionsToInstant;
  }

  /**
   * Sets the instant to retrieve versions before (exclusive).
   * If this instant equals the {@code versionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest version.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @param versionsToInstant  the new value of the property
   */
  public void setVersionsToInstant(Instant versionsToInstant) {
    this._versionsToInstant = versionsToInstant;
  }

  /**
   * Gets the the {@code versionsToInstant} property.
   * If this instant equals the {@code versionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest version.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @return the property, not null
   */
  public final Property<Instant> versionsToInstant() {
    return metaBean().versionsToInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant to retrieve corrections on or after (inclusive).
   * If this instant equals the {@code correctionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version prior to corrections.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @return the value of the property
   */
  public Instant getCorrectionsFromInstant() {
    return _correctionsFromInstant;
  }

  /**
   * Sets the instant to retrieve corrections on or after (inclusive).
   * If this instant equals the {@code correctionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version prior to corrections.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @param correctionsFromInstant  the new value of the property
   */
  public void setCorrectionsFromInstant(Instant correctionsFromInstant) {
    this._correctionsFromInstant = correctionsFromInstant;
  }

  /**
   * Gets the the {@code correctionsFromInstant} property.
   * If this instant equals the {@code correctionsToInstant} the search is at a single instant.
   * A null value will retrieve values starting from the earliest version prior to corrections.
   * This should be equal to or later than the {@code versionsFromInstant}.
   * @return the property, not null
   */
  public final Property<Instant> correctionsFromInstant() {
    return metaBean().correctionsFromInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant to retrieve corrections before (exclusive).
   * If this instant equals the {@code correctionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest correction.
   * This should be equal to or later than the {@code correctionsFromInstant}.
   * @return the value of the property
   */
  public Instant getCorrectionsToInstant() {
    return _correctionsToInstant;
  }

  /**
   * Sets the instant to retrieve corrections before (exclusive).
   * If this instant equals the {@code correctionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest correction.
   * This should be equal to or later than the {@code correctionsFromInstant}.
   * @param correctionsToInstant  the new value of the property
   */
  public void setCorrectionsToInstant(Instant correctionsToInstant) {
    this._correctionsToInstant = correctionsToInstant;
  }

  /**
   * Gets the the {@code correctionsToInstant} property.
   * If this instant equals the {@code correctionsFromInstant} the search is at a single instant.
   * A null value will retrieve values up to the latest correction.
   * This should be equal to or later than the {@code correctionsFromInstant}.
   * @return the property, not null
   */
  public final Property<Instant> correctionsToInstant() {
    return metaBean().correctionsToInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code HolidaySearchHistoricRequest}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code pagingRequest} property.
     */
    private final MetaProperty<PagingRequest> _pagingRequest = DirectMetaProperty.ofReadWrite(this, "pagingRequest", PagingRequest.class);
    /**
     * The meta-property for the {@code holidayId} property.
     */
    private final MetaProperty<UniqueIdentifier> _holidayId = DirectMetaProperty.ofReadWrite(this, "holidayId", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code versionsFromInstant} property.
     */
    private final MetaProperty<Instant> _versionsFromInstant = DirectMetaProperty.ofReadWrite(this, "versionsFromInstant", Instant.class);
    /**
     * The meta-property for the {@code versionsToInstant} property.
     */
    private final MetaProperty<Instant> _versionsToInstant = DirectMetaProperty.ofReadWrite(this, "versionsToInstant", Instant.class);
    /**
     * The meta-property for the {@code correctionsFromInstant} property.
     */
    private final MetaProperty<Instant> _correctionsFromInstant = DirectMetaProperty.ofReadWrite(this, "correctionsFromInstant", Instant.class);
    /**
     * The meta-property for the {@code correctionsToInstant} property.
     */
    private final MetaProperty<Instant> _correctionsToInstant = DirectMetaProperty.ofReadWrite(this, "correctionsToInstant", Instant.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("pagingRequest", _pagingRequest);
      temp.put("holidayId", _holidayId);
      temp.put("versionsFromInstant", _versionsFromInstant);
      temp.put("versionsToInstant", _versionsToInstant);
      temp.put("correctionsFromInstant", _correctionsFromInstant);
      temp.put("correctionsToInstant", _correctionsToInstant);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public HolidaySearchHistoricRequest createBean() {
      return new HolidaySearchHistoricRequest();
    }

    @Override
    public Class<? extends HolidaySearchHistoricRequest> beanType() {
      return HolidaySearchHistoricRequest.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code pagingRequest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PagingRequest> pagingRequest() {
      return _pagingRequest;
    }

    /**
     * The meta-property for the {@code holidayId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> holidayId() {
      return _holidayId;
    }

    /**
     * The meta-property for the {@code versionsFromInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> versionsFromInstant() {
      return _versionsFromInstant;
    }

    /**
     * The meta-property for the {@code versionsToInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> versionsToInstant() {
      return _versionsToInstant;
    }

    /**
     * The meta-property for the {@code correctionsFromInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> correctionsFromInstant() {
      return _correctionsFromInstant;
    }

    /**
     * The meta-property for the {@code correctionsToInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> correctionsToInstant() {
      return _correctionsToInstant;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}