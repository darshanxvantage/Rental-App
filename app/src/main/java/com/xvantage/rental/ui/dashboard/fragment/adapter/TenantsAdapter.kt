package com.xvantage.rental.ui.dashboard.fragment.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.HomeTenantsItemBinding
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.ui.tenant.TenantDetailsActivity
import com.xvantage.rental.utils.AppPreference

class TenantsAdapter(
    private val context: Context,
    private val isGridMode: Boolean = false,
) : RecyclerView.Adapter<TenantsAdapter.TenantDetailsViewHolder>() {

    private lateinit var appPreference: AppPreference

    private var tenantList: List<TenantItem> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(tenantList: List<TenantItem>) {
        this.tenantList = tenantList
        notifyDataSetChanged()
    }


    inner class TenantDetailsViewHolder(
        private val itemBinding: HomeTenantsItemBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {


        fun setData(data: TenantItem) {

            // Grid mode → full width
            // Horizontal mode → fixed 160dp
            val cardParams = itemBinding.root.layoutParams

            if (isGridMode) {

                cardParams.width =
                    ViewGroup.LayoutParams.MATCH_PARENT

            } else {

                cardParams.width =
                    (160 * context.resources.displayMetrics.density)
                        .toInt()
            }

            itemBinding.root.layoutParams = cardParams


            // Tenant Data
            itemBinding.tvTenantName.text =
                data.tenant_name

            itemBinding.tvLocation.text =
                data.tenant_details?.property?.name ?: "N/A"

            itemBinding.tvNumber.text =
                data.phone_number ?: "N/A"


            // Tenant Profile Image
            Glide.with(context)
                .load(data.profile_pic)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(itemBinding.itemImage)


            android.util.Log.e(
                "TENANT_STATUS",
                "Tenant = ${data.tenant_name} | Status = ${data.status}"
            )


            // Tenant Status
            val isActive =
                data.status.equals(
                    "ACTIVE",
                    ignoreCase = true
                )


            if (isActive) {

                itemBinding.tvStatus.text =
                    "Active"

                itemBinding.tvStatus
                    .setBackgroundResource(
                        R.drawable.status_background
                    )

            } else {

                itemBinding.tvStatus.text =
                    "Inactive"

                itemBinding.tvStatus
                    .setBackgroundResource(
                        R.drawable.red_status_bg
                    )
            }

            itemBinding.moreButton.setOnClickListener {

                openTenantDetails(data)
            }
            itemBinding.root.setOnClickListener {

                openTenantDetails(data)
            }
        }
        private fun openTenantDetails(
            data: TenantItem
        ) {

            val intent = Intent(
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