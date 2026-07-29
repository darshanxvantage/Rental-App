package com.xvantage.rental.ui.explore.tenant.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemTenantBinding
import com.xvantage.rental.network.response.explore.TenantResponse

class TenantAdapter(
    private val context: Context
) : RecyclerView.Adapter<TenantAdapter.TenantViewHolder>() {

    private var tenants: List<TenantResponse> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newTenants: List<TenantResponse>) {
        this.tenants = newTenants
        notifyDataSetChanged()
    }

    inner class TenantViewHolder(private val binding: ItemTenantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tenant: TenantResponse) {
            binding.tvTenantName.text = tenant.tenantName ?: "Unnamed tenant"
            binding.tvTenantProperty.text = tenant.propertyName ?: "Property unavailable"
            binding.tvTenantRoom.text = tenant.roomName ?: "-"
            binding.tvTenantRent.text = tenant.rent?.let { "₹${it.toInt()}/month" } ?: ""

            if (!tenant.profilePic.isNullOrEmpty()) {
                Glide.with(context)
                    .load(tenant.profilePic)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(binding.imgTenantAvatar)
            } else {
                binding.imgTenantAvatar.setImageResource(R.drawable.ic_profile_placeholder)
            }

            // due_amount / due_card UI intentionally left out for now -
            // to be designed and added in a later pass (per plan).
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val binding = ItemTenantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TenantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        holder.bind(tenants[position])
    }

    override fun getItemCount(): Int = tenants.size
}