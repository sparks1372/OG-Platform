/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

/**
 * Utilities around the configuration master.
 */
public final class ConfigMasterUtils {

  /**
   * Stores the document in the database ensuring a unique name.
   * <p>
   * This will read the current document with the specified name and
   * either add or update as necessary. Since the read and modify are two
   * separate steps, there is a race condition, thus this method is intended
   * for sensible setup purposes rather than ensuring uniqueness.
   * 
   * @param <T>  the configuration element type
   * @param master  the config master, not null
   * @param document  the document to store, not null
   * @return the updated result, not null
   */
  public static <T> ConfigDocument<T> storeByName(final ConfigTypeMaster<T> master, final ConfigDocument<T> document) {
    ConfigSearchRequest searchRequest = new ConfigSearchRequest();
    searchRequest.setName(document.getName());
    ConfigSearchResult<T> searchResult = master.search(searchRequest);
    for (ConfigDocument<T> existingDoc : searchResult.getDocuments()) {
      if (existingDoc.getValue().equals(document.getValue())) {
        return existingDoc;
      }
    }
    ConfigDocument<T> firstExistingDoc = searchResult.getFirstDocument();
    if (firstExistingDoc == null) {
      return master.add(document);
    } else {
      if (document.getUniqueId() == null) {
        document.setUniqueId(firstExistingDoc.getUniqueId());
      }
      return master.update(document);
    }
  }

}