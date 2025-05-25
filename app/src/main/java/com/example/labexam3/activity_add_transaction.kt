package com.example.labexam3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AddTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // ✅ Ask for POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        SharedPrefHelper.init(this)

        val etTitle: EditText = findViewById(R.id.etTitle)
        val etAmount: EditText = findViewById(R.id.etAmount)
        val spinnerCategory: Spinner = findViewById(R.id.spinnerCategory)
        val btnSave: Button = findViewById(R.id.btnSave)
        val tvCategoryTotals: TextView = findViewById(R.id.tvCategoryTotals)
        val budgetIcon: ImageView = findViewById(R.id.budgetIcon)
        val homeIcon: ImageView = findViewById(R.id.homeIcon)

        val incomeCategories = arrayOf("Salary", "Business", "Freelance", "Investments", "Other Income")
        val expenseCategories = arrayOf("Food", "Transport", "Bills", "Entertainment", "Others")

        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, expenseCategories)

        etTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateCategorySpinner(etTitle.text.toString(), spinnerCategory, incomeCategories, expenseCategories)
            }
        }

        val editIndex = intent.getIntExtra("edit_transaction_index", -1)
        val isEditMode = editIndex != -1

        if (isEditMode) {
            val editTitle = intent.getStringExtra("edit_transaction_title")
            val editAmount = intent.getDoubleExtra("edit_transaction_amount", 0.0)
            val editCategory = intent.getStringExtra("edit_transaction_category")

            etTitle.setText(editTitle)
            etAmount.setText(editAmount.toString())

            val isIncome = editTitle.equals("Income", ignoreCase = true)
            val categoriesToUse = if (isIncome) incomeCategories else expenseCategories
            spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoriesToUse)

            val categoryIndex = categoriesToUse.indexOf(editCategory)
            if (categoryIndex != -1) {
                spinnerCategory.setSelection(categoryIndex)
            }

            btnSave.text = "Update"
        }

        updateCategoryTotals(tvCategoryTotals)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = spinnerCategory.selectedItem.toString()
            val date = System.currentTimeMillis()

            if (title.isBlank() || amount == null) {
                Toast.makeText(this, "Please enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction_item(title, amount, category, date)

            if (isEditMode) {
                SharedPrefHelper.updateTransaction(editIndex, transaction)
                Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
            } else {
                SharedPrefHelper.addTransaction(transaction)
                Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()

                val totalSpent = SharedPrefHelper.getCurrentMonthExpenses()
                val budgetLimit = SharedPrefHelper.getMonthlyBudget()

                // ✅ Updated condition to include == case and log the values
                if (budgetLimit > 0 &&
                    totalSpent >= budgetLimit &&
                    !SharedPrefHelper.hasBeenNotifiedThisMonth()) {

                    Log.d("BUDGET_DEBUG", "Notification Triggered - Total Spent: $totalSpent, Budget: $budgetLimit")

                    BudgetNotificationHelper.createChannel(this)
                    BudgetNotificationHelper.showBudgetExceededNotification(this)
                    SharedPrefHelper.setNotifiedThisMonth()
                }
            }

            updateCategoryTotals(tvCategoryTotals)
            finish()
        }

        // Navigate to SetBudgetActivity when budgetIcon is clicked
        budgetIcon.setOnClickListener {
            val intent = Intent(this, SetBudgetActivity::class.java)
            startActivity(intent)
        }

        // Navigate to MainActivity when homeIcon is clicked
        homeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateCategoryTotals(tv: TextView) {
        val categoryTotals = getCategoryWiseTotals()
        val summary = categoryTotals.entries.joinToString("\n") { "${it.key}: Rs. ${it.value}" }

        Log.d("CATEGORY_TOTALS", summary)
        tv.text = summary

        updateBarChart(categoryTotals)
    }

    private fun getCategoryWiseTotals(): Map<String, Double> {
        val categoryTotals = mutableMapOf<String, Double>()
        val transactions = SharedPrefHelper.loadTransactions()

        for (transaction in transactions) {
            val category = transaction.category
            val amount = transaction.amount
            if (!transaction.title.equals("Income", ignoreCase = true)) {
                categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + amount
            }
        }

        return categoryTotals
    }

    private fun updateCategorySpinner(
        title: String,
        spinner: Spinner,
        incomeCategories: Array<String>,
        expenseCategories: Array<String>
    ) {
        val isIncome = title.trim().equals("Income", ignoreCase = true)
        val categories = if (isIncome) incomeCategories else expenseCategories
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
    }

    private fun updateBarChart(data: Map<String, Double>) {
        val barChartContainer: LinearLayout = findViewById(R.id.barChartContainer)
        barChartContainer.removeAllViews()

        if (data.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No expense data available"
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.darker_gray))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 32, 0, 0)
                }
            }
            barChartContainer.addView(emptyText)
            return
        }

        val max = data.maxOf { it.value }
        val colors = listOf(
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark,
            android.R.color.holo_green_dark,
            android.R.color.holo_red_dark,
            android.R.color.holo_purple
        )

        val header = TextView(this).apply {
            text = "Expense Distribution"
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.black))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        barChartContainer.addView(header)

        for ((index, entry) in data.entries.withIndex()) {
            val (category, amount) = entry
            val percentage = (amount / max) * 100
            val colorIndex = index % colors.size

            val card = androidx.cardview.widget.CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                radius = 12f
                elevation = 4f
                setContentPadding(16, 16, 16, 16)
            }

            val cardContent = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val categoryRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val categoryDot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                    rightMargin = 12
                }
                setBackgroundColor(resources.getColor(colors[colorIndex]))
                background.mutate().alpha = 200
            }

            val categoryText = TextView(this).apply {
                text = category
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.black))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val amountText = TextView(this).apply {
                text = "Rs. ${String.format("%.2f", amount)}"
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.black))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            categoryRow.addView(categoryDot)
            categoryRow.addView(categoryText)
            categoryRow.addView(amountText)

            val progressContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 12, 0, 0)
                }
            }

            val progressTextRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val progressText = TextView(this).apply {
                text = "${percentage.toInt()}% of total"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            progressTextRow.addView(progressText)

            val progressBarBg = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    24
                ).apply {
                    setMargins(0, 8, 0, 0)
                }
                background = resources.getDrawable(android.R.drawable.progress_horizontal)
            }

            val progressBar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (percentage * 3).toInt(),
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(resources.getColor(colors[colorIndex]))
            }

            progressBarBg.addView(progressBar)
            progressContainer.addView(progressTextRow)
            progressContainer.addView(progressBarBg)

            cardContent.addView(categoryRow)
            cardContent.addView(progressContainer)
            card.addView(cardContent)
            barChartContainer.addView(card)
        }
    }
}
