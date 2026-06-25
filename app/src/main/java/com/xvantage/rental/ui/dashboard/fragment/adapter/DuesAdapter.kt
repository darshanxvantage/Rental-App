package com.xvantage.rental.ui.dashboard.fragment.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemDueCardBinding
import com.xvantage.rental.databinding.ItemDueMonthRowBinding
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.ui.dashboard.fragment.DuesViewModel
import com.xvantage.rental.ui.takeRent.activity.ReceivePaymentActivity
import android.widget.LinearLayout
import android.widget.TextView

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
            binding.tvTenantName.text = tenant.tenant_name
            binding.tvPhone.text = tenant.phone_number ?: ""

            val room = tenant.tenant_details?.room_no ?: "—"
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
            val due = viewModel.getTotalDue(tenant)
            binding.tvDueAmount.text = "₹${due.toLong()}"
            binding.tvDueAmount.setTextColor(
                if (due > 0) Color.parseColor("#C62828")
                else Color.parseColor("#2E7D32")
            )

            // ── Monthly rent ──
            val rent = tenant.rent?.toDoubleOrNull() ?: 0.0
            binding.tvMonthlyRent.text = "₹${rent.toLong()}"

            // ── Advance ──
            val advance = tenant.advance?.toDoubleOrNull() ?: 0.0
            binding.tvAdvance.text = "₹${advance.toLong()}"

            // ── Due Status (oldest pending month) ──
            val isOverdue = viewModel.isOverdue(tenant)
            binding.tvNextDueDate.text = viewModel.getNextDueLabel(tenant)
            binding.tvNextDueDate.setTextColor(
                if (isOverdue) Color.parseColor("#C62828")
                else Color.parseColor("#E65100")
            )

            // ── History hint — show only when 2+ months pending ──
            val cycles = viewModel.getDueCyclesSorted(tenant)
            if (cycles.size >= 2) {
                binding.llHistoryHint.visibility = View.VISIBLE
                binding.tvPendingMonthsCount.text =
                    "${cycles.size} months pending — tap to view history"

                // Click → open history bottom sheet
                binding.llHistoryHint.setOnClickListener {
                    showHistoryBottomSheet(tenant)
                }
            } else {
                binding.llHistoryHint.visibility = View.GONE
            }

            // ── Electricity ──
            val (elecType, elecAmt) = viewModel.getElectricityInfo(tenant)
            binding.tvElectricityType.text = elecType
            binding.tvElectricityType.setBackgroundColor(
                when (elecType) {
                    "Fixed" -> Color.parseColor("#1565C0")
                    "Metered" -> Color.parseColor("#E65100")
                    else -> Color.parseColor("#9E9E9E")
                }
            )
            binding.tvElectricityCharge.text =
                if (elecType == "No Cost") "Owner pays" else elecAmt

            // ── Water ──
            val (waterType, waterAmt) = viewModel.getWaterInfo(tenant)
            binding.tvWaterType.text = waterType
            binding.tvWaterType.setBackgroundColor(
                when (waterType) {
                    "Fixed" -> Color.parseColor("#1565C0")
                    "Metered" -> Color.parseColor("#E65100")
                    else -> Color.parseColor("#9E9E9E")
                }
            )
            binding.tvWaterCharge.text =
                if (waterType == "No Cost") "Owner pays" else waterAmt

            // ── Total Payable ──
            binding.tvTotalPayable.text = "₹${due.toLong()}"

            // ── Collect Rent button ──
            binding.btnCollect.setOnClickListener {
                val intent = Intent(context, ReceivePaymentActivity::class.java).apply {
                    putExtra("tenantId", tenant.id)
                    putExtra("tenantName", tenant.tenant_name)
                    putExtra("roomId", tenant.room_fk)
                    putExtra("propertyName", tenant.tenant_details?.property?.name ?: "")
                    putExtra("monthlyRent", rent)
                    putExtra("electricityCharge", elecAmt)
                    putExtra("waterCharge", waterAmt)
                    putExtra("totalPayable", due)
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

        // ════════════════════════════════════════
        // HISTORY BOTTOM SHEET
        // ════════════════════════════════════════
        private fun showHistoryBottomSheet(tenant: TenantItem) {
            val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(context)
                .inflate(R.layout.bottomsheet_due_history, null)
            dialog.setContentView(view)

            // Header
            view.findViewById<TextView>(R.id.tvHistoryTenantName)?.text =
                tenant.tenant_name
            view.findViewById<TextView>(R.id.tvHistoryRoomProperty)?.text =
                "Room ${tenant.tenant_details?.room_no ?: "—"} • ${tenant.tenant_details?.property?.name ?: "—"}"
            view.findViewById<TextView>(R.id.tvHistoryTotalDue)?.text =
                "₹${viewModel.getTotalDue(tenant).toLong()}"

            // Month list
            val llMonthList = view.findViewById<LinearLayout>(R.id.llHistoryMonthList)
            llMonthList?.removeAllViews()

            val cycles = viewModel.getDueCyclesSorted(tenant)

            cycles.forEach { cycle ->
                val rowView = LayoutInflater.from(context)
                    .inflate(R.layout.item_due_month_row, llMonthList, false)

                // Month label
                rowView.findViewById<TextView>(R.id.tvMonthLabel)?.text =
                    cycle.monthLabel

                // Amount
                rowView.findViewById<TextView>(R.id.tvMonthAmount)?.text =
                    "₹${cycle.amountDue.toLong()}"

                // Overdue tag
                val overdueTag = rowView.findViewById<TextView>(R.id.tvMonthOverdueTag)
                overdueTag?.visibility =
                    if (cycle.isOverdue) View.VISIBLE else View.GONE

                // Status badge + dot color
                val statusBadge = rowView.findViewById<TextView>(R.id.tvMonthStatus)
                val dot = rowView.findViewById<View>(R.id.viewMonthDot)

                when (cycle.status.lowercase()) {
                    "pending" -> {
                        statusBadge?.text = "Pending"
                        statusBadge?.setTextColor(Color.parseColor("#E65100"))
                        statusBadge?.setBackgroundColor(Color.parseColor("#FFF3E0"))
                        dot?.setBackgroundColor(Color.parseColor("#E65100"))
                    }
                    "partial" -> {
                        statusBadge?.text = "Partial"
                        statusBadge?.setTextColor(Color.parseColor("#1565C0"))
                        statusBadge?.setBackgroundColor(Color.parseColor("#E3F2FD"))
                        dot?.setBackgroundColor(Color.parseColor("#1565C0"))
                    }
                    "paid" -> {
                        statusBadge?.text = "Paid ✓"
                        statusBadge?.setTextColor(Color.parseColor("#2E7D32"))
                        statusBadge?.setBackgroundColor(Color.parseColor("#E8F5E9"))
                        dot?.setBackgroundColor(Color.parseColor("#2E7D32"))
                    }
                    else -> {
                        statusBadge?.text = cycle.status
                        dot?.setBackgroundColor(Color.parseColor("#9E9E9E"))
                    }
                }

                llMonthList?.addView(rowView)
            }

            // Close button
            view.findViewById<MaterialButton>(R.id.btnCloseHistory)
                ?.setOnClickListener { dialog.dismiss() }

            dialog.show()
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