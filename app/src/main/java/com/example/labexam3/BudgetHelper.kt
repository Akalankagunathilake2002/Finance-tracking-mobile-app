package com.example.labexam3

import android.content.Context
import android.content.SharedPreferences

object BudgetHelper {
    private const val PREF_NAME = "budget_prefs"
    private const val KEY_BUDGET = "monthly_budget"
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setMonthlyBudget(amount: Double) {
        preferences.edit().putFloat(KEY_BUDGET, amount.toFloat()).apply()
    }

    fun getMonthlyBudget(): Double {
        return preferences.getFloat(KEY_BUDGET, 0f).toDouble()
    }
}
