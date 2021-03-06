// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.fx;
public class FXForwardSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
    	    public <T> T accept(FXForwardSecurityVisitor<T> visitor) { return visitor.visitFXForwardSecurity(this); }
  	    public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitFXForwardSecurity(this); }
  private static final long serialVersionUID = 2195953966345358029l;
  private com.opengamma.id.ExternalId _underlyingIdentifier;
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  private javax.time.calendar.ZonedDateTime _forwardDate;
  public static final String FORWARD_DATE_KEY = "forwardDate";
  private com.opengamma.id.ExternalId _region;
  public static final String REGION_KEY = "region";
  public static final String SECURITY_TYPE = "FX_FORWARD";
  public FXForwardSecurity (com.opengamma.id.ExternalId underlyingIdentifier, javax.time.calendar.ZonedDateTime forwardDate, com.opengamma.id.ExternalId region) {
    super (SECURITY_TYPE);
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    if (forwardDate == null) throw new NullPointerException ("'forwardDate' cannot be null");
    else {
      _forwardDate = forwardDate;
    }
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
  }
  protected FXForwardSecurity (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (UNDERLYING_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXForwardSecurity - field 'underlyingIdentifier' is not present");
    try {
      _underlyingIdentifier = com.opengamma.id.ExternalId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXForwardSecurity - field 'underlyingIdentifier' is not ExternalId message", e);
    }
    fudgeField = fudgeMsg.getByName (FORWARD_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXForwardSecurity - field 'forwardDate' is not present");
    try {
      _forwardDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXForwardSecurity - field 'forwardDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (REGION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXForwardSecurity - field 'region' is not present");
    try {
      _region = com.opengamma.id.ExternalId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXForwardSecurity - field 'region' is not ExternalId message", e);
    }
  }
  public FXForwardSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.ExternalIdBundle identifiers, com.opengamma.id.ExternalId underlyingIdentifier, javax.time.calendar.ZonedDateTime forwardDate, com.opengamma.id.ExternalId region) {
    super (uniqueId, name, securityType, identifiers);
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    if (forwardDate == null) throw new NullPointerException ("'forwardDate' cannot be null");
    else {
      _forwardDate = forwardDate;
    }
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
  }
  protected FXForwardSecurity (final FXForwardSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = source._underlyingIdentifier;
    }
    if (source._forwardDate == null) _forwardDate = null;
    else {
      _forwardDate = source._forwardDate;
    }
    if (source._region == null) _region = null;
    else {
      _region = source._region;
    }
  }
  public FXForwardSecurity clone () {
    return new FXForwardSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_underlyingIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _underlyingIdentifier.getClass (), com.opengamma.id.ExternalId.class);
      _underlyingIdentifier.toFudgeMsg (serializer, fudge1);
      msg.add (UNDERLYING_IDENTIFIER_KEY, null, fudge1);
    }
    if (_forwardDate != null)  {
      serializer.addToMessage (msg, FORWARD_DATE_KEY, null, _forwardDate);
    }
    if (_region != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _region.getClass (), com.opengamma.id.ExternalId.class);
      _region.toFudgeMsg (serializer, fudge1);
      msg.add (REGION_KEY, null, fudge1);
    }
  }
  public static FXForwardSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.fx.FXForwardSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.fx.FXForwardSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FXForwardSecurity (deserializer, fudgeMsg);
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
  public javax.time.calendar.ZonedDateTime getForwardDate () {
    return _forwardDate;
  }
  public void setForwardDate (javax.time.calendar.ZonedDateTime forwardDate) {
    if (forwardDate == null) throw new NullPointerException ("'forwardDate' cannot be null");
    else {
      _forwardDate = forwardDate;
    }
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
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FXForwardSecurity)) return false;
    FXForwardSecurity msg = (FXForwardSecurity)o;
    if (_underlyingIdentifier != null) {
      if (msg._underlyingIdentifier != null) {
        if (!_underlyingIdentifier.equals (msg._underlyingIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._underlyingIdentifier != null) return false;
    if (_forwardDate != null) {
      if (msg._forwardDate != null) {
        if (!_forwardDate.equals (msg._forwardDate)) return false;
      }
      else return false;
    }
    else if (msg._forwardDate != null) return false;
    if (_region != null) {
      if (msg._region != null) {
        if (!_region.equals (msg._region)) return false;
      }
      else return false;
    }
    else if (msg._region != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_underlyingIdentifier != null) hc += _underlyingIdentifier.hashCode ();
    hc *= 31;
    if (_forwardDate != null) hc += _forwardDate.hashCode ();
    hc *= 31;
    if (_region != null) hc += _region.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
