package com.xvantage.rental.ui.dashboard.fragment.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.databinding.ItemDueCardBinding
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.ui.takeRent.activity.ReceivePaymentActivity

class DuesAdapter(
    private val context: Context
) : RecyclerView.Adapter<DuesAdapter.DueViewHolder>() {

    private var list: List<TenantItem> = emptyList()

    fun submitList(newList: List<TenantItem>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class DueViewHolder(
        private val binding: ItemDueCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tenant: TenantItem) {

            // Tenant name
            binding.tvTenantName.text = tenant.tenant_name

            // Room + Property
            val room     = tenant.tenant_details?.room_no ?: "—"
            val property = tenant.tenant_details?.property?.name ?: "—"
            binding.tvRoomProperty.text = "Room $room • $property"

            // Phone
            binding.tvPhone.text = tenant.phone_number ?: ""

            // Due amount
            val due = tenant.payment_due?.toDoubleOrNull() ?: 0.0
            binding.tvDueAmount.text = "₹${due.toLong()}"
            binding.tvDueAmount.setTextColor(
                if (due > 0)
                    context.getColor(android.R.color.holo_red_dark)
                else
                    context.getColor(android.R.color.holo_green_dark)
            )

            // Monthly rent
            val rent = tenant.rent?.toDoubleOrNull() ?: 0.0
            binding.tvMonthlyRent.text = "₹${rent.toLong()}"

            // Advance
            val advance = tenant.advance?.toDoubleOrNull() ?: 0.0
            binding.tvAdvance.text = "₹${advance.toLong()}"

            // Rent start date
            binding.tvRentStartDate.text =
                if (!tenant.rent_start_date.isNullOrEmpty()) tenant.rent_start_date
                else "—"

            // Profile photo
            if (!tenant.profile_pic.isNullOrEmpty()) {
                Glide.with(context)
                    .load(tenant.profile_pic)
                    .placeholder(com.xvantage.rental.R.drawable.ic_profile_nav)
                    .error(com.xvantage.rental.R.drawable.ic_profile_nav)
                    .circleCrop()
                    .into(binding.ivProfile)
            } else {
                binding.ivProfile.setImageResource(com.xvantage.rental.R.drawable.ic_profile_nav)
            }

            // Collect Rent button
            binding.btnCollect.setOnClickListener {
                val intent = Intent(context, ReceivePaymentActivity::class.java).apply {
                    putExtra("tenantId",     tenant.id)
                    putExtra("tenantName",   tenant.tenant_name)
                    putExtra("roomId",       tenant.tenant_details?.room_no ?: "")
                    putExtra("propertyName", tenant.tenant_details?.property?.name ?: "")
                    putExtra("monthlyRent",  rent)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DueViewHolder {
        val binding = ItemDueCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DueViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}