package com.stocktics.data.settings

import android.content.Context
import android.content.SharedPreferences

data class QuantEngineWeights(
    val techWeight: Float = 0.30f,
    val mlWeight: Float = 0.25f,
    val smartMoneyWeight: Float = 0.20f,
    val regimeWeight: Float = 0.15f,
    val forensicWeight: Float = 0.10f,
    val defaultStopLoss: Float = 0.05f,
    val defaultProfitTarget: Float = 0.15f
) {
    val isAuditedOptimal: Boolean
        get() = (techWeight == 0.30f &&
                mlWeight == 0.25f &&
                smartMoneyWeight == 0.20f &&
                regimeWeight == 0.15f &&
                forensicWeight == 0.10f &&
                defaultStopLoss == 0.05f &&
                defaultProfitTarget == 0.15f)

    val totalWeightPct: Int
        get() = Math.round((techWeight + mlWeight + smartMoneyWeight + regimeWeight + forensicWeight) * 100f)
}

object QuantWeightsPreferences {
    private const val PREFS_NAME = "stocktics_quant_weights"
    private const val KEY_TECH = "weight_tech"
    private const val KEY_ML = "weight_ml"
    private const val KEY_SMART_MONEY = "weight_smart_money"
    private const val KEY_REGIME = "weight_regime"
    private const val KEY_FORENSIC = "weight_forensic"
    private const val KEY_STOP_LOSS = "param_stop_loss"
    private const val KEY_PROFIT_TARGET = "param_profit_target"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getWeights(context: Context): QuantEngineWeights {
        val prefs = getPrefs(context)
        return QuantEngineWeights(
            techWeight = prefs.getFloat(KEY_TECH, 0.30f),
            mlWeight = prefs.getFloat(KEY_ML, 0.25f),
            smartMoneyWeight = prefs.getFloat(KEY_SMART_MONEY, 0.20f),
            regimeWeight = prefs.getFloat(KEY_REGIME, 0.15f),
            forensicWeight = prefs.getFloat(KEY_FORENSIC, 0.10f),
            defaultStopLoss = prefs.getFloat(KEY_STOP_LOSS, 0.05f),
            defaultProfitTarget = prefs.getFloat(KEY_PROFIT_TARGET, 0.15f)
        )
    }

    fun saveWeights(context: Context, weights: QuantEngineWeights) {
        getPrefs(context).edit()
            .putFloat(KEY_TECH, weights.techWeight)
            .putFloat(KEY_ML, weights.mlWeight)
            .putFloat(KEY_SMART_MONEY, weights.smartMoneyWeight)
            .putFloat(KEY_REGIME, weights.regimeWeight)
            .putFloat(KEY_FORENSIC, weights.forensicWeight)
            .putFloat(KEY_STOP_LOSS, weights.defaultStopLoss)
            .putFloat(KEY_PROFIT_TARGET, weights.defaultProfitTarget)
            .apply()
    }

    fun resetToAudited(context: Context): QuantEngineWeights {
        val defaults = QuantEngineWeights()
        saveWeights(context, defaults)
        return defaults
    }
}
