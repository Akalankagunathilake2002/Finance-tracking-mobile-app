
package com.example.labexam3.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.labexam3.AddTransactionActivity
import com.example.labexam3.R
import com.example.labexam3.Transaction_item
import com.example.labexam3.SharedPrefHelper

class TransactionAdapter(
    private val transactions: MutableList<Transaction_item>,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvItem)
        val tvAmount: TextView = view.findViewById(R.id.tvItema)
        val tvCategory: TextView = view.findViewById(R.id.tvIte)
        val ivEdit: ImageView = view.findViewById(R.id.ivEdit)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_transaction_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvTitle.text = transaction.title
        holder.tvAmount.text = "Rs. ${transaction.amount}"
        holder.tvCategory.text = transaction.category

        holder.ivEdit.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra("edit_transaction_index", position)
                putExtra("edit_transaction_title", transaction.title)
                putExtra("edit_transaction_amount", transaction.amount)
                putExtra("edit_transaction_category", transaction.category)
            }
            context.startActivity(intent)
        }

        holder.ivDelete.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Yes") { _, _ ->
                    transactions.removeAt(position)
                    SharedPrefHelper.saveAll(transactions)
                    notifyDataSetChanged()
                    onDataChanged()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun getItemCount() = transactions.size
}
