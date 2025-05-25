package com.example.labexam3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class RestoreDataActivity : AppCompatActivity() {

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                restoreDataFromUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restore_data)

        SharedPrefHelper.init(this)

        val pickFileButton: Button = findViewById(R.id.btnPickFile)
        pickFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }
            filePickerLauncher.launch(intent)
        }
    }

    private fun restoreDataFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val text = inputStream?.bufferedReader().use { it?.readText() } ?: return

            val transactions = mutableListOf<Transaction_item>()
            val lines = text.lines()

            for (line in lines) {
                if (line.isBlank()) continue

                // Format: Title: ..., Amount: Rs. ..., Date: ..., Category: ...
                val parts = line.split(", ").map { it.split(": ", limit = 2) }
                val map = parts.associate { it[0].trim() to it[1].trim() }

                val title = map["Title"] ?: continue
                val amountStr = map["Amount"]?.replace("Rs.", "") ?: continue
                val amount = amountStr.toDoubleOrNull() ?: continue
                val date = map["Date"]?.toLongOrNull() ?: continue
                val category = map["Category"] ?: "Others"

                transactions.add(Transaction_item(title, amount, category, date))
            }

            SharedPrefHelper.saveAll(transactions)

            Toast.makeText(this, "✅ Data restored from text file!", Toast.LENGTH_LONG).show()
            finish() // Return to main screen
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "❌ Failed to restore text data", Toast.LENGTH_SHORT).show()
        }
    }
}
