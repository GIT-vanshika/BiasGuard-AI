import pandas as pd
import numpy as np

def calculate_demographic_parity(df: pd.DataFrame, protected_col: str, prediction_col: str):
    if protected_col not in df.columns or prediction_col not in df.columns:
        return {"score": 0.0, "is_biased": False, "group_rates": {}}
        
    rates = df.groupby(protected_col)[prediction_col].mean().to_dict()
    if not rates:
        return {"score": 0.0, "is_biased": False, "group_rates": {}}
    
    max_rate = max(rates.values())
    min_rate = min(rates.values())
    disparity = abs(max_rate - min_rate)
    
    return {
        "score": round(disparity, 4),
        "is_biased": disparity > 0.1,
        "group_rates": {str(k): round(v, 4) for k, v in rates.items()}
    }

def calculate_equal_opportunity(df: pd.DataFrame, protected_col: str, prediction_col: str, target_col: str):
    if target_col not in df.columns:
         return {"score": 0.0, "is_biased": False, "group_rates": {}}
         
    df_positive = df[df[target_col] == 1]
    if df_positive.empty:
        return {"score": 0.0, "is_biased": False, "group_rates": {}}

    rates = df_positive.groupby(protected_col)[prediction_col].mean().to_dict()
    if not rates:
        return {"score": 0.0, "is_biased": False, "group_rates": {}}
        
    max_rate = max(rates.values())
    min_rate = min(rates.values())
    disparity = abs(max_rate - min_rate)
    
    return {
        "score": round(disparity, 4),
        "is_biased": disparity > 0.1,
        "group_rates": {str(k): round(v, 4) for k, v in rates.items()}
    }

def calculate_intersectional_bias(df: pd.DataFrame, protected_cols: list, prediction_col: str) -> float:
    if len(protected_cols) < 2:
        return 0.0
    
    # Create composite column
    df['intersectional_group'] = df[protected_cols].astype(str).agg('_'.join, axis=1)
    
    rates = df.groupby('intersectional_group')[prediction_col].mean()
    if len(rates) == 0:
        return 0.0
        
    max_rate = rates.max()
    min_rate = rates.min()
    return round(abs(max_rate - min_rate), 4)

def find_proxy_features(df: pd.DataFrame, protected_col: str, excluded_cols: list) -> list:
    if df.empty or protected_col not in df.columns:
        return []
        
    correlations = {}
    try:
        protected_coded = pd.factorize(df[protected_col])[0]
        
        for col in df.columns:
            if col == protected_col or col in excluded_cols:
                continue
                
            if pd.api.types.is_numeric_dtype(df[col]):
                corr = np.corrcoef(protected_coded, df[col].fillna(0))[0, 1]
            else:
                col_coded = pd.factorize(df[col])[0]
                corr = np.corrcoef(protected_coded, col_coded)[0, 1]
                
            if not np.isnan(corr):
                correlations[col] = abs(corr)
                
        sorted_proxies = sorted(correlations.items(), key=lambda item: item[1], reverse=True)
        return [item[0] for item in sorted_proxies[:2]]
    except Exception as e:
        print(f"Proxy calculation error: {e}")
        return []
