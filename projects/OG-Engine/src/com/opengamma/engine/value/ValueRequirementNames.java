/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * A set of common names used to refer to particular computed values. These should be used by
 * {@link FunctionDefinition}s to state their required inputs and their potential outputs, in order that a common set
 * of names are used.
 * <p>
 * For names used to refer to market data, see {@link MarketDataRequirementNames}.
 */
public interface ValueRequirementNames {

  // CSOFF: Because they're names that should be known by industry practitioners.
  
  // Standard Analytic Models:
  public static final String DISCOUNT_CURVE = "DiscountCurve";
  public static final String YIELD_CURVE = "YieldCurve";
  public static final String VOLATILITY_SURFACE = "VolatilitySurface";  
  
  //
  public static final String FAIR_VALUE = "FairValue";
  public static final String POSITION_FAIR_VALUE = "PositionFairValue";
  public static final String VALUE_FAIR_VALUE = "ValueFairValue";
  
  // Greeks Names:
  public static final String DELTA = "Delta";
  public static final String DELTA_BLEED = "DeltaBleed";
  public static final String STRIKE_DELTA = "StrikeDelta";
  public static final String DRIFTLESS_DELTA = "DriftlessTheta";
  
  public static final String GAMMA = "Gamma";
  public static final String GAMMA_P = "GammaP";
  public static final String STRIKE_GAMMA = "StrikeGamma";
  public static final String GAMMA_BLEED = "GammaBleed";
  public static final String GAMMA_P_BLEED = "GammaPBleed";
  
  public static final String VEGA = "Vega";
  public static final String VEGA_P = "VegaP";
  public static final String VARIANCE_VEGA = "VarianceVega";
  public static final String VEGA_BLEED = "VegaBleed";
  
  public static final String THETA = "Theta";
  
  public static final String RHO = "Rho";
  public static final String CARRY_RHO = "CarryRho";
  
  public static final String ZETA = "Zeta";
  public static final String ZETA_BLEED = "ZetaBleed";
  public static final String DZETA_DVOL = "dZeta_dVol";
  
  public static final String ELASTICITY = "Elasticity";
  public static final String PHI = "Phi";
  
  public static final String ZOMMA = "Zomma";
  public static final String ZOMMA_P = "ZommaP";
  
  public static final String ULTIMA = "Ultima";
  public static final String VARIANCE_ULTIMA = "VarianceUltima";
  
  public static final String SPEED = "Speed";
  public static final String SPEED_P = "SpeedP";
  
  public static final String VANNA = "Vanna";
  public static final String VARIANCE_VANNA = "VarianceVanna";
  public static final String DVANNA_DVOL = "dVanna_dVol";
  
  public static final String VOMMA = "Vomma";
  public static final String VOMMA_P = "VommaP";
  public static final String VARIANCE_VOMMA = "VarianceVomma";

  // Greeks Names:
  public static final String POSITION_DELTA = "PositionDelta";
  public static final String POSITION_DELTA_BLEED = "PositionDeltaBleed";
  public static final String POSITION_STRIKE_DELTA = "PositionStrikeDelta";
  public static final String POSITION_DRIFTLESS_DELTA = "PositionDriftlessTheta";
  
  public static final String POSITION_GAMMA = "PositionGamma";
  public static final String POSITION_GAMMA_P = "PositionGammaP";
  public static final String POSITION_STRIKE_GAMMA = "PositionStrikeGamma";
  public static final String POSITION_GAMMA_BLEED = "PositionGammaBleed";
  public static final String POSITION_GAMMA_P_BLEED = "PositionGammaPBleed";
  
  public static final String POSITION_VEGA = "PositionVega";
  public static final String POSITION_VEGA_P = "PositionVegaP";
  public static final String POSITION_VARIANCE_VEGA = "PositionVarianceVega";
  public static final String POSITION_VEGA_BLEED = "PositionVegaBleed";
  
  public static final String POSITION_THETA = "PositionTheta";
  
  public static final String POSITION_RHO = "PositionRho";
  public static final String POSITION_CARRY_RHO = "PositionCarryRho";
  
  public static final String POSITION_ZETA = "PositionZeta";
  public static final String POSITION_ZETA_BLEED = "PositionZetaBleed";
  public static final String POSITION_DZETA_DVOL = "PositiondZeta_dVol";
  
  public static final String POSITION_ELASTICITY = "PositionElasticity";
  public static final String POSITION_PHI = "PositionPhi";
  
  public static final String POSITION_ZOMMA = "PositionZomma";
  public static final String POSITION_ZOMMA_P = "PositionZommaP";
  
  public static final String POSITION_ULTIMA = "PositionUltima";
  public static final String POSITION_VARIANCE_ULTIMA = "PositionVarianceUltima";
  
  public static final String POSITION_SPEED = "PositionSpeed";
  public static final String POSITION_SPEED_P = "PositionSpeedP";
  
  public static final String POSITION_VANNA = "PositionVanna";
  public static final String POSITION_VARIANCE_VANNA = "PositionVarianceVanna";
  public static final String POSITION_DVANNA_DVOL = "PositiondVanna_dVol";
  
  public static final String POSITION_VOMMA = "PositionVomma";
  public static final String POSITION_VOMMA_P = "PositionVommaP";
  public static final String POSITION_VARIANCE_VOMMA = "PositionVarianceVomma";

  // Greeks Names:
  public static final String VALUE_DELTA = "ValueDelta";
  public static final String VALUE_DELTA_BLEED = "ValueDeltaBleed";
  public static final String VALUE_STRIKE_DELTA = "ValueStrikeDelta";
  public static final String VALUE_DRIFTLESS_DELTA = "ValueDriftlessTheta";
  
  public static final String VALUE_GAMMA = "ValueGamma";
  public static final String VALUE_GAMMA_P = "ValueGammaP";
  public static final String VALUE_STRIKE_GAMMA = "ValueStrikeGamma";
  public static final String VALUE_GAMMA_BLEED = "ValueGammaBleed";
  public static final String VALUE_GAMMA_P_BLEED = "ValueGammaPBleed";
  
  public static final String VALUE_VEGA = "ValueVega";
  public static final String VALUE_VEGA_P = "ValueVegaP";
  public static final String VALUE_VARIANCE_VEGA = "ValueVarianceVega";
  public static final String VALUE_VEGA_BLEED = "ValueVegaBleed";
  
  public static final String VALUE_THETA = "ValueTheta";
  
  public static final String VALUE_RHO = "ValueRho";
  public static final String VALUE_CARRY_RHO = "ValueCarryRho";
  
  public static final String VALUE_ZETA = "ValueZeta";
  public static final String VALUE_ZETA_BLEED = "ValueZetaBleed";
  public static final String VALUE_DZETA_DVOL = "ValuedZeta_dVol";
  
  public static final String VALUE_ELASTICITY = "ValueElasticity";
  public static final String VALUE_PHI = "ValuePhi";
  
  public static final String VALUE_ZOMMA = "ValueZomma";
  public static final String VALUE_ZOMMA_P = "ValueZommaP";
  
  public static final String VALUE_ULTIMA = "ValueUltima";
  public static final String VALUE_VARIANCE_ULTIMA = "ValueVarianceUltima";
  
  public static final String VALUE_SPEED = "ValueSpeed";
  public static final String VALUE_SPEED_P = "ValueSpeedP";
  
  public static final String VALUE_VANNA = "ValueVanna";
  public static final String VALUE_VARIANCE_VANNA = "ValueVarianceVanna";
  public static final String VALUE_DVANNA_DVOL = "ValuedVanna_dVol";
  
  public static final String VALUE_VOMMA = "ValueVomma";
  public static final String VALUE_VOMMA_P = "ValueVommaP";
  public static final String VALUE_VARIANCE_VOMMA = "ValueVarianceVomma";

  // Generic Aggregates:
  public static final String SUM = "Sum";
  public static final String MEDIAN = "Median";
  
  // History
  public static final String PRICE_SERIES = "Price Series";
  public static final String RETURN_SERIES = "Return Series";
  public static final String PNL = "P&L";
  public static final String PNL_SERIES = "P&L Series";
  public static final String UNDERLYING_RETURN_SERIES = "Underlying Return Series";
  
  // Risk Aggregates:
  public static final String HISTORICAL_VAR = "HistoricalVaR";
  public static final String ISOLATED_VAR = "IsolatedVaR";
  public static final String INCREMENTAL_VAR = "IncrementalVaR";

  //Yield curve specifics
  public static final String YIELD_CURVE_JACOBIAN = "YieldCurveJacobian";
  
  // Fixed income analytics
  public static final String PRESENT_VALUE = "PresentValue";
  
  //CAPM regression equity model
  public static final String CAPM_BETA = "CAPM Beta";

  //CAPM regression equity model
  public static final String CAPM_REGRESSION_ALPHA = "CAPM Regression Alpha";
  public static final String CAPM_REGRESSION_BETA = "CAPM Regression Regression Beta";
  public static final String CAPM_REGRESSION_ALPHA_RESIDUALS= "CAPM Regression Regression Alpha Residual";
  public static final String CAPM_REGRESSION_BETA_RESIDUALS= "CAPM Regression Regression Beta Residuals";
  public static final String CAPM_REGRESSION_ADJUSTED_R_SQUARED = "CAPM Regression Regression Adjusted R-Squared";
  public static final String CAPM_REGRESSION_ALPHA_TSTATS = "CAPM Regression Regression Alpha t-Stats";
  public static final String CAPM_REGRESSION_BETA_TSTATS = "CAPM Regression Regression Beta t-Stats";
  public static final String CAPM_REGRESSION_ALPHA_PVALUES = "CAPM Regression Regression Alpha p-Values";
  public static final String CAPM_REGRESSION_BETA_PVALUES = "CAPM Regression Regression Beta p-Values";
  public static final String CAPM_REGRESSION_MEAN_SQUARE_ERROR = "CAPM Regression Regression Mean Square Error";
  public static final String CAPM_REGRESSION_R_SQUARED = "CAPM Regression Regression R-Squared";
  public static final String CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA = "CAPM Regression Regression Standard Error Of Alpha";
  public static final String CAPM_REGRESSION_STANDARD_ERROR_OF_BETA = "CAPM Regression Regression Standard Error Of Beta";
  
  //Portfolio
  public static final String WEIGHT = "Weight";
  public static final String SHARPE_RATIO = "Sharpe Ratio";
  public static final String TREYNOR_RATIO = "Treynor Ratio";
}