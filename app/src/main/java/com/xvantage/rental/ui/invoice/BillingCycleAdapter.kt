package com.xvantage.rental.ui.invoice

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xvantage.rental.R
import com.xvantage.rental.data.model.BillingCycle
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class BillingCycleAdapter(
    private val onCycleClicked: (BillingCycle) -> Unit
) : RecyclerView.Adapter<BillingCycleAdapter.CycleViewHolder>() {

    private val items = mutableListOf<BillingCycle>()

    fun submitList(newItems: List<BillingCycle>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CycleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_billing_cycle, parent, false)
        return CycleViewHolder(view, onCycleClicked)
    }

    override fun onBindViewHolder(holder: CycleViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CycleViewHolder(
        itemView: View,
        private val onCycleClicked: (BillingCycle) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvMonth: TextView = itemView.findViewById(R.id.tvMonth)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        private val monthInFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val monthOutFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        private val currencyFormat = NumberFormat.getInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }

        fun bind(cycle: BillingCycle) {
            val monthLabel = try {
                monthOutFormat.format(monthInFormat.parse(cycle.cycleMonth)!!)
            } catch (e: Exception) {
                cycle.cycleMonth
            }
            tvMonth.text = monthLabel
            tvDueDate.text = "Due: ${cycle.dueDate}"
            tvAmount.text = "Rs. ${currencyFormat.format(cycle.totalAmount)}"

            tvStatus.text = cycle.status.replaceFirstChar { it.uppercase() }
            val (bgColor, textColor) = when (cycle.status.lowercase()) {
                "paid" -> Pair(Color.parseColor("#1E8E5A"), Color.WHITE)
                "partial" -> Pair(Color.parseColor("#C97A1E"), Color.WHITE)
                else -> Pair(Color.parseColor("#C0392B"), Color.WHITE) // pending
            }
            tvStatus.setBackgroundColor(bgColor)
            tvStatus.setTextColor(textColor)

            itemView.setOnClickListener { onCycleClicked(cycle) }
        }
    }
}