/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;

/**
 * A simple mutable implementation of a source of configuration documents.
 * <p>
 * This class is intended for testing scenarios.
 * It is not thread-safe and must not be used in production.
 */
public class MockConfigSource implements ConfigSource {
  // this is public to allow testing

  /**
   * The configuration documents keyed by identifier.
   */
  private final Map<UniqueIdentifier, ConfigDocument<?>> _configs = new HashMap<UniqueIdentifier, ConfigDocument<?>>();
  /**
   * The next index for the identifier.
   */
  private final UniqueIdentifierSupplier _uidSupplier;

  /**
   * Creates the instance.
   */
  public MockConfigSource() {
    _uidSupplier = new UniqueIdentifierSupplier("Mock");
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> search(Class<T> clazz, ConfigSearchRequest request) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(request, "request");
    Pattern matchName = RegexUtils.wildcardsToPattern(request.getName());
    List<T> result = new ArrayList<T>();
    for (ConfigDocument<?> doc : _configs.values()) {
      if (matchName.matcher(doc.getName()).matches() && clazz.isInstance(doc.getValue())) {
        result.add((T) doc.getValue());
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T searchLatest(final Class<T> clazz, final String name) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(name, "name");
    Pattern matchName = RegexUtils.wildcardsToPattern(name);
    for (ConfigDocument<?> doc : _configs.values()) {
      if (matchName.matcher(doc.getName()).matches() && clazz.isInstance(doc.getValue())) {
        return (T) doc.getValue();
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Class<T> clazz, UniqueIdentifier uid) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(uid, "uid");
    ConfigDocument<?> config = _configs.get(uid);
    if (clazz.isInstance(config.getValue())) {
      return (T) config.getValue();
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a config document to the master.
   * @param configDoc  the config document to add, not null
   */
  public void add(ConfigDocument<?> configDoc) {
    ArgumentChecker.notNull(configDoc, "doc");
    _configs.put(_uidSupplier.get(), configDoc);
  }

}
