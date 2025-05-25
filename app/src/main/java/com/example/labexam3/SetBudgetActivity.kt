package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetBudgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        val etBudget: EditText = findViewById(R.id.etBudgett)
        val btnSave: Button = findViewById(R.id.btnSaveBudgett)

        BudgetHelper.init(this)
        SharedPrefHelper.init(this)

        // 🔸 Handle save budget button
        btnSave.setOnClickListener {
            val budgetText = etBudget.text.toString()
            if (budgetText.isNotEmpty()) {
                val amount = budgetText.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    BudgetHelper.setMonthlyBudget(amount)
                    SharedPrefHelper.setMonthlyBudget(amount) // 🔸 Save to SharedPreferences
                    Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a budget", Toast.LENGTH_SHORT).show()
            }
        }


        val homeIcon: ImageView = findViewById(R.id.homeIcon)
        homeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        val addIcon: ImageView = findViewById(R.id.addIcon)
        addIcon.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }
}
