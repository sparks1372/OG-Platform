/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.google.common.collect.Lists;
import com.opengamma.livedata.resolver.JmsTopicNameResolver;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * 
 *
 * @author pietari
 */
public class NormalizationRuleSet {
  private final String _id;
  private final String _jmsTopicSuffix;
  private final List<NormalizationRule> _rules;
  
  /* Useful for tests */
  public NormalizationRuleSet(String id) {
    this(id, id, Collections.<NormalizationRule>emptyList()); 
  }
  
  /* Also useful for tests */
  public NormalizationRuleSet(String id, NormalizationRule... rules) {
    this(id, id, Lists.newArrayList(rules));
  }
  
  public NormalizationRuleSet(String id, 
      String jmsTopicSuffix,
      List<NormalizationRule> rules) {
    ArgumentChecker.notNull(id, "Rule set ID");
    ArgumentChecker.notNull(jmsTopicSuffix, "Jms Topic Suffix");
    ArgumentChecker.notNull(rules, "StandardRules");
    _id = id;
    
    if (!jmsTopicSuffix.isEmpty() && !jmsTopicSuffix.startsWith(JmsTopicNameResolver.SEPARATOR)) {
      _jmsTopicSuffix = JmsTopicNameResolver.SEPARATOR + jmsTopicSuffix;
    } else {
      _jmsTopicSuffix = jmsTopicSuffix;
    }
    
    _rules = new ArrayList<NormalizationRule>(rules);    
  }
  
  public FudgeFieldContainer getNormalizedMessage(
      FudgeFieldContainer msg,
      FieldHistoryStore fieldHistory) {
    MutableFudgeFieldContainer normalizedMsg = OpenGammaFudgeContext.getInstance().newMessage(msg);
    for (NormalizationRule rule : _rules) {
      normalizedMsg = rule.apply(normalizedMsg, fieldHistory);
      if (normalizedMsg == null) {
        // One of the rules rejected the message entirely.
        break;
      }
    }
    return normalizedMsg;
  }
  
  public String getId() {
    return _id;
  }
  
  /**
   * Return value, if non-empty, will always start with {@link JmsTopicNameResolver#SEPARATOR}.
   * However, an empty string is also a possibility.
   * 
   * @return the JMS topic suffix
   */
  public String getJmsTopicSuffix() {
    return _jmsTopicSuffix;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_id == null) ? 0 : _id.hashCode());
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
    NormalizationRuleSet other = (NormalizationRuleSet) obj;
    if (_id == null) {
      if (other._id != null) {
        return false;
      }
    } else if (!_id.equals(other._id)) {
      return false;
    }
    return true;
  }
  
}