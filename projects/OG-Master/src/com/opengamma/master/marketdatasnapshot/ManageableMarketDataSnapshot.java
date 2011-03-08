/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.core.marketdatasnapshot.FXVolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * A snapshot of market data taken at a particular time and potentially altered by hand
 */
@BeanDefinition
@PublicSPI
public class ManageableMarketDataSnapshot extends DirectBean implements MarketDataSnapshot {

  /**
   * The unique identifier of the snapshot.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueId;
  
  /**
   * The name of the snapshot intended for display purposes.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private String _name;

  /**
   * The individual market data points in this snapshot
   * NOTE: An individual data point can have a different value here and in any 
   *        structured object in which it appears (e.g. yield curve)
   */
  @PropertyDefinition
  private Map<Identifier, ValueSnapshot> _values;

  /**
   * The yield curves in this snapshot
   */
  @PropertyDefinition
  private Map<Pair<String, CurrencyUnit>, YieldCurveSnapshot> _yieldCurves;

  /**
   * The FX volatility surfaces in this snapshot
   */
  @PropertyDefinition
  private Map<Triple<String, CurrencyUnit, CurrencyUnit>, FXVolatilitySurfaceSnapshot> _fxVolatilitySurfaces;
  

  /**
   * Creates a snapshot
   */
  public ManageableMarketDataSnapshot() {
    super();
  }
  
  /**
   * Creates a snapshot
   * @param name the name of the snapshot
   * @param values the individual market data points
   * @param yieldCurves the yield curves
   * @param fxVolatilitySurfaces the FX volatility surfaces
   */
  public ManageableMarketDataSnapshot(String name, Map<Identifier, ValueSnapshot> values,
      Map<Pair<String, CurrencyUnit>, YieldCurveSnapshot> yieldCurves,
      Map<Triple<String, CurrencyUnit, CurrencyUnit>, FXVolatilitySurfaceSnapshot> fxVolatilitySurfaces) {
    super();
    _name = name;
    _values = values;
    _yieldCurves = yieldCurves;
    _fxVolatilitySurfaces = fxVolatilitySurfaces;
  }


  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableMarketDataSnapshot}.
   * @return the meta-bean, not null
   */
  public static ManageableMarketDataSnapshot.Meta meta() {
    return ManageableMarketDataSnapshot.Meta.INSTANCE;
  }

  @Override
  public ManageableMarketDataSnapshot.Meta metaBean() {
    return ManageableMarketDataSnapshot.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case 3373707:  // name
        return getName();
      case -823812830:  // values
        return getValues();
      case 119589713:  // yieldCurves
        return getYieldCurves();
      case -791071459:  // fxVolatilitySurfaces
        return getFxVolatilitySurfaces();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueIdentifier) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case -823812830:  // values
        setValues((Map<Identifier, ValueSnapshot>) newValue);
        return;
      case 119589713:  // yieldCurves
        setYieldCurves((Map<Pair<String, CurrencyUnit>, YieldCurveSnapshot>) newValue);
        return;
      case -791071459:  // fxVolatilitySurfaces
        setFxVolatilitySurfaces((Map<Triple<String, CurrencyUnit, CurrencyUnit>, FXVolatilitySurfaceSnapshot>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the snapshot.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the snapshot.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the snapshot intended for display purposes.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the snapshot intended for display purposes.
   * This field must not be null for the object to be valid.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * This field must not be null for the object to be valid.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the individual market data points in this snapshot
   * NOTE: An individual data point can have a different value here and in any
   * structured object in which it appears (e.g. yield curve)
   * @return the value of the property
   */
  public Map<Identifier, ValueSnapshot> getValues() {
    return _values;
  }

  /**
   * Sets the individual market data points in this snapshot
   * NOTE: An individual data point can have a different value here and in any
   * structured object in which it appears (e.g. yield curve)
   * @param values  the new value of the property
   */
  public void setValues(Map<Identifier, ValueSnapshot> values) {
    this._values = values;
  }

  /**
   * Gets the the {@code values} property.
   * NOTE: An individual data point can have a different value here and in any
   * structured object in which it appears (e.g. yield curve)
   * @return the property, not null
   */
  public final Property<Map<Identifier, ValueSnapshot>> values() {
    return metaBean().values().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the yield curves in this snapshot
   * @return the value of the property
   */
  public Map<Pair<String, CurrencyUnit>, YieldCurveSnapshot> getYieldCurves() {
    return _yieldCurves;
  }

  /**
   * Sets the yield curves in this snapshot
   * @param yieldCurves  the new value of the property
   */
  public void setYieldCurves(Map<Pair<String, CurrencyUnit>, YieldCurveSnapshot> yieldCurves) {
    this._yieldCurves = yieldCurves;
  }

  /**
   * Gets the the {@code yieldCurves} property.
   * @return the property, not null
   */
  public final Property<Map<Pair<String, CurrencyUnit>, YieldCurveSnapshot>> yieldCurves() {
    return metaBean().yieldCurves().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the FX volatility surfaces in this snapshot
   * @return the value of the property
   */
  public Map<Triple<String, CurrencyUnit, CurrencyUnit>, FXVolatilitySurfaceSnapshot> getFxVolatilitySurfaces() {
    return _fxVolatilitySurfaces;
  }

  /**
   * Sets the FX volatility surfaces in this snapshot
   * @param fxVolatilitySurfaces  the new value of the property
   */
  public void setFxVolatilitySurfaces(Map<Triple<String, CurrencyUnit, CurrencyUnit>, FXVolatilitySurfaceSnapshot> fxVolatilitySurfaces) {
    this._fxVolatilitySurfaces = fxVolatilitySurfaces;
  }

  /**
   * Gets the the {@code fxVolatilitySurfaces} property.
   * @return the property, not null
   */
  public final Property<Map<Triple<String, CurrencyUnit, CurrencyUnit>, FXVolatilitySurfaceSnapshot>> fxVolatilitySurfaces() {
    return metaBean().fxVolatilitySurfaces().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableMarketDataSnapshot}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueId = DirectMetaProperty.ofReadWrite(this, "uniqueId", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-property for the {@code values} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Identifier, ValueSnapshot>> _values = DirectMetaProperty.ofReadWrite(this, "values", (Class) Map.class);
    /**
     * The meta-property for the {@code yieldCurves} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Pair<String, CurrencyUnit>, YieldCurveSnapshot>> _yieldCurves = DirectMetaProperty.ofReadWrite(this, "yieldCurves", (Class) Map.class);
    /**
     * The meta-property for the {@code fxVolatilitySurfaces} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Triple<String, CurrencyUnit, CurrencyUnit>, FXVolatilitySurfaceSnapshot>> _fxVolatilitySurfaces = DirectMetaProperty.ofReadWrite(this, "fxVolatilitySurfaces", (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("uniqueId", _uniqueId);
      temp.put("name", _name);
      temp.put("values", _values);
      temp.put("yieldCurves", _yieldCurves);
      temp.put("fxVolatilitySurfaces", _fxVolatilitySurfaces);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public ManageableMarketDataSnapshot createBean() {
      return new ManageableMarketDataSnapshot();
    }

    @Override
    public Class<? extends ManageableMarketDataSnapshot> beanType() {
      return ManageableMarketDataSnapshot.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code values} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<Identifier, ValueSnapshot>> values() {
      return _values;
    }

    /**
     * The meta-property for the {@code yieldCurves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<Pair<String, CurrencyUnit>, YieldCurveSnapshot>> yieldCurves() {
      return _yieldCurves;
    }

    /**
     * The meta-property for the {@code fxVolatilitySurfaces} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<Triple<String, CurrencyUnit, CurrencyUnit>, FXVolatilitySurfaceSnapshot>> fxVolatilitySurfaces() {
      return _fxVolatilitySurfaces;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}