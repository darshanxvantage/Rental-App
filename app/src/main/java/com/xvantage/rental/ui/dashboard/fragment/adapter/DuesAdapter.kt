package com.xvantage.rental.ui.dashboard.fragment.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemDueCardBinding
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.ui.dashboard.fragment.DuesViewModel
import com.xvantage.rental.ui.takeRent.activity.ReceivePaymentActivity

class DuesAdapter(
    private val context: Context,
    private val viewModel: DuesViewModel
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

            // ── Tenant basic info ──
            binding.tvTenantName.text   = tenant.tenant_name
            binding.tvPhone.text        = tenant.phone_number ?: ""

            val room     = tenant.tenant_details?.room_no ?: "—"
            val property = tenant.tenant_details?.property?.name ?: "—"
            binding.tvRoomProperty.text = "Room $room • $property"

            // ── Profile photo ──
            if (!tenant.profile_pic.isNullOrEmpty()) {
                Glide.with(context)
                    .load(tenant.profile_pic)
                    .placeholder(R.drawable.ic_profile_nav)
                    .error(R.drawable.ic_profile_nav)
                    .circleCrop()
                    .into(binding.ivProfile)
            } else {
                binding.ivProfile.setImageResource(R.drawable.ic_profile_nav)
            }

            // ── Due amount ──
            val due = tenant.payment_due?.toDoubleOrNull() ?: 0.0
            binding.tvDueAmount.text = "₹${due.toLong()}"
            binding.tvDueAmount.setTextColor(
                if (due > 0) Color.parseColor("#C62828")
                else         Color.parseColor("#2E7D32")
            )

            // ── Monthly rent ──
            val rent = tenant.rent?.toDoubleOrNull() ?: 0.0
            binding.tvMonthlyRent.text = "₹${rent.toLong()}"

            // ── Advance ──
            val advance = tenant.advance?.toDoubleOrNull() ?: 0.0
            binding.tvAdvance.text = "₹${advance.toLong()}"

            // ── Next Due Date (payment cycle logic) ──
            val nextDue    = viewModel.getNextDueDate(tenant)
            val isOverdue  = viewModel.isOverdue(tenant)
            binding.tvNextDueDate.text      = nextDue
            binding.tvNextDueDate.setTextColor(
                if (isOverdue) Color.parseColor("#C62828")   // red = overdue
                else           Color.parseColor("#E65100")   // orange = upcoming
            )

            // ── Electricity ──
            val (elecType, elecAmt) = viewModel.getElectricityInfo(tenant)
            binding.tvElectricityType.text   = elecType
            binding.tvElectricityCharge.text = elecAmt

            // Badge color: No Cost=grey, Fixed=blue, Metered=orange
            binding.tvElectricityType.setBackgroundColor(
                when (elecType) {
                    "Fixed"   -> Color.parseColor("#1565C0")
                    "Metered" -> Color.parseColor("#E65100")
                    else      -> Color.parseColor("#9E9E9E")
                }
            )
            // Hide amount row if no cost
            binding.tvElectricityCharge.text =
                if (elecType == "No Cost") "Owner pays" else elecAmt

            // ── Water ──
            val (waterType, waterAmt) = viewModel.getWaterInfo(tenant)
            binding.tvWaterType.text   = waterType
            binding.tvWaterCharge.text = waterAmt

            binding.tvWaterType.setBackgroundColor(
                when (waterType) {
                    "Fixed"   -> Color.parseColor("#1565C0")
                    "Metered" -> Color.parseColor("#E65100")
                    else      -> Color.parseColor("#9E9E9E")
                }
            )
            binding.tvWaterCharge.text =
                if (waterType == "No Cost") "Owner pays" else waterAmt

            // ── Total Payable ──
            val total = viewModel.getTotalPayable(tenant)
            binding.tvTotalPayable.text = "₹$total"

            // ── Collect Rent button ──
            binding.btnCollect.setOnClickListener {
                val intent = Intent(context, ReceivePaymentActivity::class.java).apply {
                    putExtra("tenantId",      tenant.id)
                    putExtra("tenantName",    tenant.tenant_name)
                    putExtra(
                        "roomId",
                        tenant.room_fk
                    )
                    putExtra("propertyName",  tenant.tenant_details?.property?.name ?: "")
                    putExtra("monthlyRent",   rent)
                    putExtra("electricityCharge", elecAmt)
                    putExtra("waterCharge",        waterAmt)
                    putExtra("totalPayable",       total.toDouble())
                    putExtra("electricityMode", tenant.fixed_electricity ?: "")
                    putExtra("waterMode", tenant.fixed_waterbill ?: "")
                    putExtra("lastMeterReading", tenant.last_meter_reading ?: "")
                    putExtra("lastWaterReading", tenant.last_meter_reading_water ?: "")
                    putExtra("costPerUnit", tenant.cost_per_unit ?: "")
                    putExtra("costUnitWater", tenant.cost_unit_water ?: "")
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