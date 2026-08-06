package com.xvantage.rental.ui.dashboard.fragment.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemDueCardBinding
import com.xvantage.rental.network.response.DueCycle
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.ui.dashboard.fragment.DuesViewModel

class DuesAdapter(
    private val context: Context,
    private val viewModel: DuesViewModel,
    private val onCollectClick: (TenantItem) -> Unit
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

            // ── Basic info ──
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
            binding.tvDueAmount.text = formatAmount(due)
            binding.tvDueAmount.setTextColor(
                if (due > 0) Color.parseColor("#C62828")
                else Color.parseColor("#2E7D32")
            )

            // ── Monthly rent ──
            val rent = tenant.rent?.toDoubleOrNull() ?: 0.0
            binding.tvMonthlyRent.text = formatAmount(rent)

            // ── Advance ──
            val advance = tenant.advance?.toDoubleOrNull() ?: 0.0
            binding.tvAdvance.text = formatAmount(advance)

            // ── Due Status ──
            val isOverdue = viewModel.isOverdue(tenant)
            binding.tvNextDueDate.text = viewModel.getNextDueLabel(tenant)
            binding.tvNextDueDate.setTextColor(
                if (isOverdue) Color.parseColor("#C62828")
                else Color.parseColor("#E65100")
            )

            // ── Due Soon alert banner ──
            val cyclesForAlert = viewModel.getDueCyclesSorted(tenant)
            val soonCycle = cyclesForAlert.firstOrNull { it.isDueSoon && !it.isOverdue }
            if (!isOverdue && viewModel.shouldAlertDueSoon(tenant) && soonCycle != null) {
                binding.llDueSoonAlert.visibility = View.VISIBLE
                binding.tvDueSoonMessage.text =
                    "Rent due ${viewModel.getNextDueLabel(tenant).lowercase()} — collect before due date"
            } else {
                binding.llDueSoonAlert.visibility = View.GONE
            }

            val cycles = viewModel.getDueCyclesSorted(tenant)
            val firstCycle = cycles.firstOrNull()


            if (cycles.isNotEmpty()) {
                binding.tvBillingPeriod.visibility = View.VISIBLE
                val periodStart = getCycleStartDate(tenant, cycles.first())
                val periodEnd = getCycleEndDate(cycles.last())
                binding.tvBillingPeriod.text = if (cycles.size == 1) {
                    "Billing period: ${formatFullDate(periodStart)} – ${formatFullDate(periodEnd)}"
                } else {
                    "Billing period: ${formatFullDate(periodStart)} – ${formatFullDate(periodEnd)} (${cycles.size} months)"
                }
            } else {
                binding.tvBillingPeriod.visibility = View.GONE
            }

            if (firstCycle != null && firstCycle.isProrated == true) {
                binding.llProratedInfo.visibility = View.VISIBLE
                val days = firstCycle.proratedDays ?: 0
                val proratedStart = getCycleStartDate(tenant, firstCycle)
                val proratedEnd = getCycleEndDate(firstCycle)
                binding.tvProratedLabel.text =
                    "First month (${formatFullDate(proratedStart)} – ${formatFullDate(proratedEnd)}): $days days billing (prorated)"
                binding.tvProratedAmount.text = formatAmount(firstCycle.totalAmount)
            } else {
                binding.llProratedInfo.visibility = View.GONE
            }


            val allCycles = viewModel.getAllCyclesSorted(tenant)
            if (allCycles.isNotEmpty()) {
                binding.llHistoryHint.visibility = View.VISIBLE
                binding.tvPendingMonthsCount.text = when {
                    cycles.isEmpty() -> "View payment history"
                    cycles.size == 1 -> "1 month pending — tap to view history"
                    else -> "${cycles.size} months pending — tap to view history"
                }
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
            binding.tvTotalPayable.text = formatAmount(due)

            // ── Collect Rent ──
            binding.btnCollect.setOnClickListener {

                onCollectClick(tenant)

            }
        }

        private fun showHistoryBottomSheet(tenant: TenantItem) {
            val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(context)
                .inflate(R.layout.bottomsheet_due_history, null)
            dialog.setContentView(view)

            view.findViewById<TextView>(R.id.tvHistoryTenantName)?.text = tenant.tenant_name
            view.findViewById<TextView>(R.id.tvHistoryRoomProperty)?.text =
                "Room ${tenant.tenant_details?.room_no ?: "—"} • ${tenant.tenant_details?.property?.name ?: "—"}"
            view.findViewById<TextView>(R.id.tvHistoryTotalDue)?.text =
                formatAmount(viewModel.getTotalDue(tenant))

            val llMonthList = view.findViewById<LinearLayout>(R.id.llHistoryMonthList)
            llMonthList?.removeAllViews()

            val cycles = viewModel.getAllCyclesSorted(tenant)
            cycles.forEach { cycle ->
                val rowView = LayoutInflater.from(context)
                    .inflate(R.layout.item_due_month_row, llMonthList, false)

                var monthLabel = cycle.monthLabel
                rowView.findViewById<TextView>(R.id.tvMonthLabel)?.text = monthLabel
                rowView.findViewById<TextView>(R.id.tvMonthDateRange)?.text =
                    "${formatFullDate(getCycleStartDate(tenant, cycle))} – ${formatFullDate(getCycleEndDate(cycle))}"
                rowView.findViewById<TextView>(R.id.tvMonthAmount)?.text =
                    formatAmount(cycle.amountDue)

                val overdueTag = rowView.findViewById<TextView>(R.id.tvMonthOverdueTag)

                when {
                    cycle.isOverdue -> {
                        overdueTag?.text = "⚠ Overdue"
                        overdueTag?.visibility = View.VISIBLE
                    }
                    cycle.isDueSoon -> {
                        overdueTag?.text = "⏰ Due Soon"
                        overdueTag?.setTextColor(Color.parseColor("#E65100"))
                        overdueTag?.visibility = View.VISIBLE
                    }
                    else -> {
                        overdueTag?.visibility = View.GONE
                    }
                }

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

            view.findViewById<MaterialButton>(R.id.btnCloseHistory)
                ?.setOnClickListener { dialog.dismiss() }

            dialog.show()
        }
    }

    private fun getCycleStartDate(tenant: TenantItem, cycle: DueCycle): String {
        return if (cycle.isProrated && !tenant.rent_start_date.isNullOrBlank()) {
            tenant.rent_start_date!!
        } else {
            cycle.cycleMonth
        }
    }

    /** The last calendar day of a billing cycle's month. */
    private fun getCycleEndDate(cycle: DueCycle): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            cal.time = sdf.parse(cycle.cycleMonth) ?: return cycle.cycleMonth
            cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
            sdf.format(cal.time)
        } catch (e: Exception) {
            cycle.cycleMonth
        }
    }

    /** Formats "yyyy-MM-dd" into a readable "6 Aug 2026" style date. */
    private fun formatFullDate(dateStr: String): String {
        return try {
            val input = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val output = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
            val date = input.parse(dateStr)
            if (date != null) output.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }


    private fun formatAmount(amount: Double): String {
        val rounded = Math.round(amount * 100.0) / 100.0
        return if (rounded == Math.floor(rounded)) {
            "₹${rounded.toLong()}"
        } else {
            "₹${String.format(java.util.Locale.US, "%.2f", rounded)}"
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