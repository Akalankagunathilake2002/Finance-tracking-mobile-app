package com.example.labexam3

import android.content.Context
import android.content.SharedPreferences
import com.example.labexam3.Transaction_item
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object SharedPrefHelper {
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("finance_prefs", Context.MODE_PRIVATE)
    }

    fun addTransaction(transaction: Transaction_item) {
        val transactions = loadTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    fun loadTransactions(): List<Transaction_item> {
        val json = sharedPreferences.getString("transactions", null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction_item>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveTransactions(transactions: List<Transaction_item>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString("transactions", json).apply()
    }

    fun updateTransaction(index: Int, transaction: Transaction_item) {
        val transactions = loadTransactions().toMutableList()
        if (index in transactions.indices) {
            transactions[index] = transaction
            saveTransactions(transactions)
        }
    }

    fun saveAll(transactions: List<Transaction_item>) {
        saveTransactions(transactions)
    }


    fun setMonthlyBudget(budget: Double) {
        sharedPreferences.edit().putFloat("monthly_budget", budget.toFloat()).apply()
    }

    fun getMonthlyBudget(): Double {
        return sharedPreferences.getFloat("monthly_budget", 0f).toDouble()
    }

    fun getCurrentMonthExpenses(): Double {
        val transactions = loadTransactions()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return transactions.filter {
            val transCalendar = Calendar.getInstance().apply { timeInMillis = it.date }
            transCalendar.get(Calendar.MONTH) == currentMonth &&
                    transCalendar.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }


    fun hasBeenNotifiedThisMonth(): Boolean {
        val notifiedMonth = sharedPreferences.getInt("notified_month", -1)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        return notifiedMonth == currentMonth
    }

    fun setNotifiedThisMonth() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        sharedPreferences.edit().putInt("notified_month", currentMonth).apply()
    }
}
