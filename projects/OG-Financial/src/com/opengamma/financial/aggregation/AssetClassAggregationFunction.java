/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Function to classify positions by asset class.  Note that this bins all types of options together.
 * For more detailed subdivision, see DetailedAssetClassAggregationFunction.
 * @author jim
 */
public class AssetClassAggregationFunction implements AggregationFunction<String> {
  /* package */static final String FX_OPTIONS = "FX Options";
  /* package */static final String FX_BARRIER_OPTIONS = "FX Barrier Options";
  /* package */static final String BONDS = "Bonds";
  /* package */static final String CASH = "Cash";
  /* package */static final String EQUITIES = "Equities";
  /* package */static final String FRAS = "FRAs";
  /* package */static final String FUTURES = "Futures";
  /* package */static final String EQUITY_INDEX_OPTIONS = "Equity Index Options";
  /* package */static final String EQUITY_OPTIONS = "Equity Options";
  /* package */static final String EQUITY_VARIANCE_SWAPS = "Equity Variance Swaps";
  /* package */static final String SWAPTIONS = "Swaptions";
  /* package */static final String IRFUTURE_OPTIONS = "IRFuture Options";
  /* package */static final String SWAPS = "Swaps";
  /* package */static final String FX = "FX";
  /* package */static final String FX_FORWARDS = "FX Forwards";
  /* package */static final String CAP_FLOOR = "Cap/Floor";
  /* package */static final String CAP_FLOOR_CMS_SPREAD = "Cap/Floor CMS Spread";
  /* package */static final String UNKNOWN = "Unknown Security Type";
  /* package */static final String NAME = "Asset Class";

  @Override
  public String classifyPosition(final Position position) {
    final Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(new FinancialSecurityVisitor<String>() {

        @Override
        public String visitBondSecurity(final BondSecurity security) {
          return BONDS;
        }

        @Override
        public String visitCashSecurity(final CashSecurity security) {
          return CASH;
        }

        @Override
        public String visitEquitySecurity(final EquitySecurity security) {
          return EQUITIES;
        }

        @Override
        public String visitFRASecurity(final FRASecurity security) {
          return FRAS;
        }

        @Override
        public String visitFutureSecurity(final FutureSecurity security) {
          return FUTURES;
        }

        @Override
        public String visitSwapSecurity(final SwapSecurity security) {
          return SWAPS;
        }

        @Override
        public String visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return EQUITY_INDEX_OPTIONS;
        }

        @Override
        public String visitEquityOptionSecurity(final EquityOptionSecurity equityOptionSecurity) {
          return EQUITY_OPTIONS;
        }

        @Override
        public String visitFXOptionSecurity(final FXOptionSecurity fxOptionSecurity) {
          return FX_OPTIONS;
        }

        @Override
        public String visitSwaptionSecurity(final SwaptionSecurity security) {
          return SWAPTIONS;
        }

        @Override
        public String visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return IRFUTURE_OPTIONS;
        }

        @Override
        public String visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          return FX_BARRIER_OPTIONS;
        }

        @Override
        public String visitFXSecurity(final FXSecurity security) {
          return FX;
        }

        @Override
        public String visitFXForwardSecurity(final FXForwardSecurity security) {
          return FX_FORWARDS;
        }

        @Override
        public String visitCapFloorSecurity(final CapFloorSecurity security) {
          return CAP_FLOOR;
        }

        @Override
        public String visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return CAP_FLOOR_CMS_SPREAD;
        }

        @Override
        public String visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
          return EQUITY_VARIANCE_SWAPS;
        }

      });
    } else {
      return UNKNOWN;
    }
  }

  @Override
  public String getName() {
    return NAME;
  }
}
