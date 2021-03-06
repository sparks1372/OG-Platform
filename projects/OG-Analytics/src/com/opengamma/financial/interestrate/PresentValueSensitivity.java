/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a present value curve sensitivity.
 */
public class PresentValueSensitivity {

  /**
   * The map containing the sensitivity. The map linked the curve (String) to a list of pairs (cash flow time, sensitivity value).
   */
  private final Map<String, List<DoublesPair>> _sensitivity;

  /**
   * Default constructor, creating an empty HashMap for the sensitivity.
   */
  public PresentValueSensitivity() {
    _sensitivity = new HashMap<String, List<DoublesPair>>();
  }

  /**
   * Constructor from a map of sensitivity.
   * @param sensitivity The map.
   */
  public PresentValueSensitivity(Map<String, List<DoublesPair>> sensitivity) {
    Validate.notNull(sensitivity, "sensitivity");
    this._sensitivity = sensitivity;
  }

  /**
   * Gets the sensitivity map.
   * @return The sensitivity map
   */
  public Map<String, List<DoublesPair>> getSensitivities() {
    return _sensitivity;
  }

  /**
   * Create a copy of the sensitivity and add a given sensitivity to it.
   * @param other The sensitivity to add.
   * @return The total sensitivity.
   */
  public PresentValueSensitivity add(PresentValueSensitivity other) {
    Validate.notNull(other, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : _sensitivity.keySet()) {
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : _sensitivity.get(name)) {
        temp.add(pair);
      }
      if (other.getSensitivities().containsKey(name)) {
        for (final DoublesPair pair : other._sensitivity.get(name)) {
          temp.add(pair);
        }
      }
      result.put(name, temp);
    }
    for (final String name : other.getSensitivities().keySet()) {
      if (!result.containsKey(name)) {
        final List<DoublesPair> temp = new ArrayList<DoublesPair>();
        for (final DoublesPair pair : other._sensitivity.get(name)) {
          temp.add(pair);
        }
        result.put(name, temp);
      }
    }
    return new PresentValueSensitivity(result);
  }

  /**
   * Create a new sensitivity object containing the original sensitivity multiplied by a common factor.
   * @param factor The multiplicative factor.
   * @return The multiplied sensitivity.
   */
  public PresentValueSensitivity multiply(double factor) {
    Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : _sensitivity.keySet()) {
      final List<DoublesPair> curveSensi = new ArrayList<DoublesPair>();
      for (final DoublesPair pair : _sensitivity.get(name)) {
        curveSensi.add(new DoublesPair(pair.first, pair.second * factor));
      }
      result.put(name, curveSensi);
    }
    return new PresentValueSensitivity(result);
  }

  /**
   * Return a clean sensitivity by sorting the times and adding the duplicate times.
   * @return The cleaned sensitivity.
   */
  public PresentValueSensitivity clean() {
    //TODO: improve the sorting algorithm.
    Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : _sensitivity.keySet()) {
      List<DoublesPair> list = _sensitivity.get(name);
      List<DoublesPair> listClean = new ArrayList<DoublesPair>();
      Set<Double> set = new TreeSet<Double>();
      for (final DoublesPair pair : list) {
        set.add(pair.getFirst());
      }
      for (Double time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          if (list.get(looplist).getFirst().doubleValue() == time.doubleValue()) {
            sensi += list.get(looplist).second;
          }
        }
        listClean.add(new DoublesPair(time, sensi));
      }
      result.put(name, listClean);
    }
    return new PresentValueSensitivity(result);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sensitivity.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PresentValueSensitivity other = (PresentValueSensitivity) obj;
    if (!ObjectUtils.equals(_sensitivity, other._sensitivity)) {
      return false;
    }
    return true;
  }

}
