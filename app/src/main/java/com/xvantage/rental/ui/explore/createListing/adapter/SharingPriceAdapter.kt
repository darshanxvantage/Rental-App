package com.xvantage.rental.ui.explore.createListing.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xvantage.rental.databinding.ItemSharingPriceInputBinding
import com.xvantage.rental.network.request.explore.SharingPriceItem

class SharingPriceAdapter(
    initialRows: List<SharingPriceItem> = emptyList()
) : RecyclerView.Adapter<SharingPriceAdapter.RowViewHolder>() {

    private val rows: MutableList<SharingPriceItem> = if (initialRows.isNotEmpty()) {
        initialRows.toMutableList()
    } else {
        mutableListOf(SharingPriceItem(sharingType = "single", price = 0.0, availableBeds = 0))
    }

    fun getAllRows(): List<SharingPriceItem> = rows.toList()

    @SuppressLint("NotifyDataSetChanged")
    fun addEmptyRow() {
        rows.add(SharingPriceItem(sharingType = "single", price = 0.0, availableBeds = 0))
        notifyItemInserted(rows.size - 1)
    }

    fun removeAt(position: Int) {
        if (rows.size <= 1) return // always keep at least one row
        if (position !in rows.indices) return
        rows.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, rows.size - position)
    }

    inner class RowViewHolder(private val binding: ItemSharingPriceInputBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var priceWatcher: TextWatcher? = null
        private var depositWatcher: TextWatcher? = null
        private var maintenanceWatcher: TextWatcher? = null
        private var bedsWatcher: TextWatcher? = null

        fun bind(row: SharingPriceItem, position: Int) {
            binding.tvSharingTypeInputLabel.text = "Sharing Option ${position + 1}"

            // ---- sharing type chips ----
            when (row.sharingType) {
                "single" -> binding.chipRowSingle.isChecked = true
                "double" -> binding.chipRowDouble.isChecked = true
                "triple" -> binding.chipRowTriple.isChecked = true
                "four_sharing" -> binding.chipRowFourSharing.isChecked = true
                "dormitory" -> binding.chipRowDormitory.isChecked = true
            }
            val chipClickListener: (android.view.View) -> Unit = {
                val newType = when (it.id) {
                    binding.chipRowSingle.id -> "single"
                    binding.chipRowDouble.id -> "double"
                    binding.chipRowTriple.id -> "triple"
                    binding.chipRowFourSharing.id -> "four_sharing"
                    binding.chipRowDormitory.id -> "dormitory"
                    else -> row.sharingType
                }
                updateRow(position) { copy(sharingType = newType) }
            }
            binding.chipRowSingle.setOnClickListener(chipClickListener)
            binding.chipRowDouble.setOnClickListener(chipClickListener)
            binding.chipRowTriple.setOnClickListener(chipClickListener)
            binding.chipRowFourSharing.setOnClickListener(chipClickListener)
            binding.chipRowDormitory.setOnClickListener(chipClickListener)

            // ---- text fields (remove old watchers first to avoid duplicate callbacks on recycle) ----
            priceWatcher?.let { binding.etRowPrice.removeTextChangedListener(it) }
            depositWatcher?.let { binding.etRowDeposit.removeTextChangedListener(it) }
            maintenanceWatcher?.let { binding.etRowMaintenance.removeTextChangedListener(it) }
            bedsWatcher?.let { binding.etRowAvailableBeds.removeTextChangedListener(it) }

            binding.etRowPrice.setText(if (row.price > 0) row.price.toInt().toString() else "")
            binding.etRowDeposit.setText(row.securityDeposit?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
            binding.etRowMaintenance.setText(row.maintenanceCharge?.let { if (it > 0) it.toInt().toString() else "" } ?: "")
            binding.etRowAvailableBeds.setText(if (row.availableBeds > 0) row.availableBeds.toString() else "")

            priceWatcher = simpleWatcher { text ->
                updateRow(position) { copy(price = text.toDoubleOrNull() ?: 0.0) }
            }
            depositWatcher = simpleWatcher { text ->
                updateRow(position) { copy(securityDeposit = text.toDoubleOrNull()) }
            }
            maintenanceWatcher = simpleWatcher { text ->
                updateRow(position) { copy(maintenanceCharge = text.toDoubleOrNull()) }
            }
            bedsWatcher = simpleWatcher { text ->
                updateRow(position) { copy(availableBeds = text.toIntOrNull() ?: 0) }
            }

            binding.etRowPrice.addTextChangedListener(priceWatcher)
            binding.etRowDeposit.addTextChangedListener(depositWatcher)
            binding.etRowMaintenance.addTextChangedListener(maintenanceWatcher)
            binding.etRowAvailableBeds.addTextChangedListener(bedsWatcher)

            binding.btnRemoveSharingRow.setOnClickListener {
                removeAt(adapterPosition)
            }
        }

        private fun simpleWatcher(onChanged: (String) -> Unit): TextWatcher {
            return object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    onChanged(s?.toString().orEmpty())
                }
            }
        }
    }

    private fun updateRow(position: Int, transform: SharingPriceItem.() -> SharingPriceItem) {
        if (position !in rows.indices) return
        rows[position] = rows[position].transform()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val binding = ItemSharingPriceInputBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bind(rows[position], position)
    }

    override fun getItemCount(): Int = rows.size
}