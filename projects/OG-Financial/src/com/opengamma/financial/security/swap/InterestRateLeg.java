// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/financial/security/swap/SwapLeg.proto:24(19)
package com.opengamma.financial.security.swap;
public abstract class InterestRateLeg extends com.opengamma.financial.security.swap.SwapLeg implements java.io.Serializable {
  private static final long serialVersionUID = 1l;
  public InterestRateLeg (com.opengamma.financial.convention.daycount.DayCount dayCount, com.opengamma.financial.convention.frequency.Frequency frequency, com.opengamma.id.Identifier regionIdentifier, com.opengamma.financial.convention.businessday.BusinessDayConvention businessDayConvention, com.opengamma.financial.security.swap.Notional notional) {
    super (dayCount, frequency, regionIdentifier, businessDayConvention, notional);
  }
  protected InterestRateLeg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
  }
  protected InterestRateLeg (final InterestRateLeg source) {
    super (source);
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
  }
  public static InterestRateLeg fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.InterestRateLeg".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.InterestRateLeg)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("InterestRateLeg is an abstract message");
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof InterestRateLeg)) return false;
    InterestRateLeg msg = (InterestRateLeg)o;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File