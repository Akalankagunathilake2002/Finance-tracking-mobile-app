package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.labexam3.adapters.TransactionAdapter
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var transactionList: ArrayList<Transaction_item>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var tvTotal: TextView
    private lateinit var spinnerCurrency: Spinner
    private var currentCurrency: String = "Rs."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BudgetNotificationHelper.createChannel(this)

        val btnAddTransaction: Button = findViewById(R.id.btnAddTransactionn)
        val btnDownload: Button = findViewById(R.id.btnDownload)
        val btnSetBudget: Button = findViewById(R.id.btnSetBudget)
        val btnRestore: Button = findViewById(R.id.btnRestore)
        tvTotal = findViewById(R.id.tvTotall)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerVieww)
        spinnerCurrency = findViewById(R.id.spinnerCurrency)

        val budgetIcon: ImageView = findViewById(R.id.budgetIcon)
        val addIcon: ImageView = findViewById(R.id.addIcon)

        budgetIcon.setOnClickListener {
            startActivity(Intent(this, SetBudgetActivity::class.java))
        }

        addIcon.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        setupCurrencySpinner()

        SharedPrefHelper.init(this)
        transactionList = ArrayList(SharedPrefHelper.loadTransactions())

        transactionAdapter = TransactionAdapter(transactionList) {
            updateTotalDisplay()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = transactionAdapter

        btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        btnDownload.setOnClickListener {
            val fileName = "transactions_backup.txt"
            val content = buildString {
                for (transaction in transactionList) {
                    append("Title: ${transaction.title}, Amount: Rs.${transaction.amount}, Date: ${transaction.date}, Category: ${transaction.category}\n")
                }
            }
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            try {
                file.writeText(content)
                Toast.makeText(this, "Transaction Downloads", Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Backup failed", Toast.LENGTH_SHORT).show()
            }
        }

        btnSetBudget.setOnClickListener {
            startActivity(Intent(this, SetBudgetActivity::class.java))
        }

        // ✅ Launch RestoreDataActivity instead of file picker
        btnRestore.setOnClickListener {
            val intent = Intent(this, RestoreDataActivity::class.java)
            startActivity(intent)
        }

        updateTotalDisplay()
    }

    private fun setupCurrencySpinner() {
        val currencies = arrayOf("Rs.", "$", "€", "£", "¥")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = adapter

        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                currentCurrency = currencies[position]
                updateTotalDisplay()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        transactionList.clear()
        transactionList.addAll(SharedPrefHelper.loadTransactions())
        transactionAdapter.notifyDataSetChanged()
        updateTotalDisplay()
    }

    private fun calculateTotal(): Double {
        var total = 0.0
        for (transaction in transactionList) {
            if (transaction.title.equals("Income", ignoreCase = true)) {
                total += transaction.amount
            } else if (transaction.title.equals("Expense", ignoreCase = true)) {
                total -= transaction.amount
            }
        }
        return total
    }

    private fun updateTotalDisplay() {
        tvTotal.text = "Total: $currentCurrency${calculateTotal()}"
    }
}
