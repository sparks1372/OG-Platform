/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;

import com.opengamma.util.PublicSPI;

/**
 * Utilities around the configuration master.
 */
@PublicSPI
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
  public static <T> ConfigDocument<T> storeByName(final ConfigMaster master, final ConfigDocument<T> document) {

    final int maxRetries = 10; //IGN-101 This is so high because the tests hammer this function with the same name
    int retries = 0;

    if (document.getUniqueId() == null) {
      while (true) {
        try {
          return storeByNameInner(master, document);
        } catch (IllegalArgumentException ex) {
          if (++retries == maxRetries) {
            throw ex;
          }
        } catch (IncorrectUpdateSemanticsDataAccessException ex) {
          if (++retries == maxRetries) {
            throw ex;
          }
        }

        document.setUniqueId(null);
      }
    } else {
      return storeByNameInner(master, document);
    }
  }

  private static <T> ConfigDocument<T> storeByNameInner(final ConfigMaster master, final ConfigDocument<T> document) {
    ConfigSearchRequest<T> searchRequest = new ConfigSearchRequest<T>();
    searchRequest.setType(document.getType());
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
