/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Fudge secondary type for {@code Currency} converting to a string.
 */
public final class CurrencySecondaryType extends SecondaryFieldType<Currency, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final CurrencySecondaryType INSTANCE = new CurrencySecondaryType();

  /** Serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * Restricted constructor.
   */
  private CurrencySecondaryType() {
    super(FudgeWireType.STRING, Currency.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public String secondaryToPrimary(Currency object) {
    return object.getCode();
  }

  @Override
  public Currency primaryToSecondary(final String isoCodeOrUniqueId) {
    if (isoCodeOrUniqueId.length() == 3) {
      // 3 letters means ISO code
      return Currency.of(isoCodeOrUniqueId);
    } else {
      // Otherwise, try as a UID
      final UniqueId uniqueId = UniqueId.parse(isoCodeOrUniqueId);
      if (Currency.OBJECT_SCHEME.equals(uniqueId.getScheme())) {
        return Currency.of(uniqueId.getValue());
      } else {
        throw new IllegalArgumentException("Not a unique identifier or currency ISO code - '"
            + isoCodeOrUniqueId + "'");
      }
    }
  }

}
