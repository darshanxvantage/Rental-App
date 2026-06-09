package com.xvantage.rental.ui.dashboard.fragment.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.HomeTenantsItemBinding
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.utils.AppPreference
import android.widget.Toast
import android.content.Intent
import com.xvantage.rental.ui.tenant.TenantDetailsActivity

class TenantsAdapter(
    private val context: Context,
) : RecyclerView.Adapter<TenantsAdapter.TenantDetailsViewHolder>() {

    private lateinit var appPreference: AppPreference

    private var tenantList: List<TenantItem> =
        emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(tenantList: List<TenantItem>) {

        this.tenantList = tenantList

        notifyDataSetChanged()
    }

    inner class TenantDetailsViewHolder(
        private val itemBinding: HomeTenantsItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun setData(data: TenantItem) {

            itemBinding.tvTenantName.text =
                data.tenant_name

            itemBinding.tvLocation.text =
                data.tenant_details?.property?.name ?: "N/A"

            itemBinding.tvNumber.text =
                data.phone_number ?: "N/A"

            // Profile Image
            Glide.with(context)
                .load(data.profile_pic)
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .into(itemBinding.itemImage)

            android.util.Log.e(
                "TENANT_STATUS",
                "Tenant = ${data.tenant_name} Status = ${data.status}"
            )

            // Status
            if (
                data.status.equals(
                    "ACTIVE",
                    true
                )
            ) {

                itemBinding.tvStatus.text =
                    "🟢 Active"

            } else {

                itemBinding.tvStatus.text =
                    "🔴 Inactive"
            }

            itemBinding.moreButton.setOnClickListener {

                val intent =
                    Intent(
                        context,
                        TenantDetailsActivity::class.java
                    )

                intent.putExtra(
                    "tenantId",
                    data.id
                )

                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TenantDetailsViewHolder {

        val binding =
            HomeTenantsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return TenantDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TenantDetailsViewHolder,
        position: Int
    ) {

        appPreference =
            AppPreference(context)

        holder.setData(
            tenantList[position]
        )
    }

    override fun getItemCount(): Int =
        tenantList.size
}