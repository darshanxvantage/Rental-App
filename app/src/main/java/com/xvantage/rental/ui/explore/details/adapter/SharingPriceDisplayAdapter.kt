package com.xvantage.rental.ui.explore.details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xvantage.rental.databinding.ItemSharingPriceRowBinding
import com.xvantage.rental.network.response.explore.SharingPriceResponse
import com.xvantage.rental.ui.explore.common.ExploreConstants
import java.text.NumberFormat
import java.util.Locale

class SharingPriceDisplayAdapter(
    private val sharingPrices: List<SharingPriceResponse>
) : RecyclerView.Adapter<SharingPriceDisplayAdapter.RowViewHolder>() {

    private val inrFormat = NumberFormat.getInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }

    inner class RowViewHolder(private val binding: ItemSharingPriceRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(row: SharingPriceResponse) {
            binding.tvSharingType.text = ExploreConstants.sharingTypeLabel(row.sharingType)
            binding.tvSharingPrice.text = "₹${inrFormat.format(row.price)}/mo"

            binding.tvAvailableBeds.text = if (row.availableBeds > 0) {
                "${row.availableBeds} bed${if (row.availableBeds > 1) "s" else ""} available"
            } else {
                "Currently full"
            }

            binding.tvDeposit.text = row.securityDeposit?.let {
                "Deposit: ₹${inrFormat.format(it)}"
            } ?: "No deposit info"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val binding = ItemSharingPriceRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bind(sharingPrices[position])
    }

    override fun getItemCount(): Int = sharingPrices.size
}