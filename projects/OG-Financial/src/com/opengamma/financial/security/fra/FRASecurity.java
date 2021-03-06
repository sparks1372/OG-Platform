// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.fra;
public class FRASecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public <T> T accept(FRASecurityVisitor<T> visitor) { return visitor.visitFRASecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitFRASecurity(this); }
  private static final long serialVersionUID = -2848496775655682215l;
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  private com.opengamma.id.ExternalId _region;
  public static final String REGION_KEY = "region";
  private javax.time.calendar.ZonedDateTime _startDate;
  public static final String START_DATE_KEY = "startDate";
  private javax.time.calendar.ZonedDateTime _endDate;
  public static final String END_DATE_KEY = "endDate";
  private double _rate;
  public static final String RATE_KEY = "rate";
  private double _amount;
  public static final String AMOUNT_KEY = "amount";
  private com.opengamma.id.ExternalId _underlyingIdentifier;
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  public static final String SECURITY_TYPE = "FRA";
  public FRASecurity (com.opengamma.util.money.Currency currency, com.opengamma.id.ExternalId region, javax.time.calendar.ZonedDateTime startDate, javax.time.calendar.ZonedDateTime endDate, double rate, double amount, com.opengamma.id.ExternalId underlyingIdentifier) {
    super (SECURITY_TYPE);
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
    if (startDate == null) throw new NullPointerException ("'startDate' cannot be null");
    else {
      _startDate = startDate;
    }
    if (endDate == null) throw new NullPointerException ("'endDate' cannot be null");
    else {
      _endDate = endDate;
    }
    _rate = rate;
    _amount = amount;
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
  }
  protected FRASecurity (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'currency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (REGION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'region' is not present");
    try {
      _region = com.opengamma.id.ExternalId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'region' is not ExternalId message", e);
    }
    fudgeField = fudgeMsg.getByName (START_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'startDate' is not present");
    try {
      _startDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'startDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (END_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'endDate' is not present");
    try {
      _endDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'endDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (RATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'rate' is not present");
    try {
      _rate = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'rate' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (AMOUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'amount' is not present");
    try {
      _amount = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'amount' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (UNDERLYING_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'underlyingIdentifier' is not present");
    try {
      _underlyingIdentifier = com.opengamma.id.ExternalId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FRASecurity - field 'underlyingIdentifier' is not ExternalId message", e);
    }
  }
  public FRASecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.ExternalIdBundle identifiers, com.opengamma.util.money.Currency currency, com.opengamma.id.ExternalId region, javax.time.calendar.ZonedDateTime startDate, javax.time.calendar.ZonedDateTime endDate, double rate, double amount, com.opengamma.id.ExternalId underlyingIdentifier) {
    super (uniqueId, name, securityType, identifiers);
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
    if (startDate == null) throw new NullPointerException ("'startDate' cannot be null");
    else {
      _startDate = startDate;
    }
    if (endDate == null) throw new NullPointerException ("'endDate' cannot be null");
    else {
      _endDate = endDate;
    }
    _rate = rate;
    _amount = amount;
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
  }
  protected FRASecurity (final FRASecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _currency = source._currency;
    if (source._region == null) _region = null;
    else {
      _region = source._region;
    }
    if (source._startDate == null) _startDate = null;
    else {
      _startDate = source._startDate;
    }
    if (source._endDate == null) _endDate = null;
    else {
      _endDate = source._endDate;
    }
    _rate = source._rate;
    _amount = source._amount;
    if (source._underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = source._underlyingIdentifier;
    }
  }
  public FRASecurity clone () {
    return new FRASecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
    if (_region != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _region.getClass (), com.opengamma.id.ExternalId.class);
      _region.toFudgeMsg (serializer, fudge1);
      msg.add (REGION_KEY, null, fudge1);
    }
    if (_startDate != null)  {
      serializer.addToMessage (msg, START_DATE_KEY, null, _startDate);
    }
    if (_endDate != null)  {
      serializer.addToMessage (msg, END_DATE_KEY, null, _endDate);
    }
    msg.add (RATE_KEY, null, _rate);
    msg.add (AMOUNT_KEY, null, _amount);
    if (_underlyingIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _underlyingIdentifier.getClass (), com.opengamma.id.ExternalId.class);
      _underlyingIdentifier.toFudgeMsg (serializer, fudge1);
      msg.add (UNDERLYING_IDENTIFIER_KEY, null, fudge1);
    }
  }
  public static FRASecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.fra.FRASecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.fra.FRASecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FRASecurity (deserializer, fudgeMsg);
  }
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public void setCurrency (com.opengamma.util.money.Currency currency) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  public com.opengamma.id.ExternalId getRegion () {
    return _region;
  }
  public void setRegion (com.opengamma.id.ExternalId region) {
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
  }
  public javax.time.calendar.ZonedDateTime getStartDate () {
    return _startDate;
  }
  public void setStartDate (javax.time.calendar.ZonedDateTime startDate) {
    if (startDate == null) throw new NullPointerException ("'startDate' cannot be null");
    else {
      _startDate = startDate;
    }
  }
  public javax.time.calendar.ZonedDateTime getEndDate () {
    return _endDate;
  }
  public void setEndDate (javax.time.calendar.ZonedDateTime endDate) {
    if (endDate == null) throw new NullPointerException ("'endDate' cannot be null");
    else {
      _endDate = endDate;
    }
  }
  public double getRate () {
    return _rate;
  }
  public void setRate (double rate) {
    _rate = rate;
  }
  public double getAmount () {
    return _amount;
  }
  public void setAmount (double amount) {
    _amount = amount;
  }
  public com.opengamma.id.ExternalId getUnderlyingIdentifier () {
    return _underlyingIdentifier;
  }
  public void setUnderlyingIdentifier (com.opengamma.id.ExternalId underlyingIdentifier) {
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FRASecurity)) return false;
    FRASecurity msg = (FRASecurity)o;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    if (_region != null) {
      if (msg._region != null) {
        if (!_region.equals (msg._region)) return false;
      }
      else return false;
    }
    else if (msg._region != null) return false;
    if (_startDate != null) {
      if (msg._startDate != null) {
        if (!_startDate.equals (msg._startDate)) return false;
      }
      else return false;
    }
    else if (msg._startDate != null) return false;
    if (_endDate != null) {
      if (msg._endDate != null) {
        if (!_endDate.equals (msg._endDate)) return false;
      }
      else return false;
    }
    else if (msg._endDate != null) return false;
    if (_rate != msg._rate) return false;
    if (_amount != msg._amount) return false;
    if (_underlyingIdentifier != null) {
      if (msg._underlyingIdentifier != null) {
        if (!_underlyingIdentifier.equals (msg._underlyingIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._underlyingIdentifier != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    hc *= 31;
    if (_region != null) hc += _region.hashCode ();
    hc *= 31;
    if (_startDate != null) hc += _startDate.hashCode ();
    hc *= 31;
    if (_endDate != null) hc += _endDate.hashCode ();
    hc = (hc * 31) + (int)_rate;
    hc = (hc * 31) + (int)_amount;
    hc *= 31;
    if (_underlyingIdentifier != null) hc += _underlyingIdentifier.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
