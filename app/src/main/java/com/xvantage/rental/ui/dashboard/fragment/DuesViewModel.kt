package com.xvantage.rental.ui.dashboard.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DuesViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val allTenants = MutableStateFlow<List<TenantItem>>(emptyList())
    val isLoading  = MutableStateFlow(false)
    val errorMsg   = MutableStateFlow<String?>(null)

    fun loadDues() {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = repository.getTenantDues()) {
                is ResultWrapper.Success -> {
                    allTenants.value = result.value.data.tenants
                }
                is ResultWrapper.Error -> {
                    errorMsg.value = result.message
                }
                else -> {}
            }
            isLoading.value = false
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

    fun getElectricityInfo(tenant: TenantItem): Pair<String, String> {
        val mode = tenant.fixed_electricity?.trim()?.lowercase()
        val currentCycle = tenant.dueCycles?.firstOrNull()

        return when (mode) {
            "no cost", "no_cost", "none" -> Pair("No Cost", "₹0")
            "fix", "fixed" -> {
                val amt = currentCycle?.electricityAmount
                    ?: tenant.fixed_electricity_amount?.toDoubleOrNull()
                    ?: 0.0
                Pair("Fixed", formatAmount(amt))
            }
            "meter", "metered" -> Pair("Metered", "Per usage")
            else -> Pair("No Cost", "₹0")
        }
    }

    fun getWaterInfo(tenant: TenantItem): Pair<String, String> {
        val mode = tenant.fixed_waterbill?.trim()?.lowercase()
        val currentCycle = tenant.dueCycles?.firstOrNull()

        return when (mode) {
            "no cost", "no_cost", "none" -> Pair("No Cost", "₹0")
            "fix", "fixed" -> {
                val amt = currentCycle?.waterAmount
                    ?: tenant.fixed_waterbill_amount?.toDoubleOrNull()
                    ?: 0.0
                Pair("Fixed", formatAmount(amt))
            }
            "meter", "metered" -> Pair("Metered", "Per usage")
            else -> Pair("No Cost", "₹0")
        }
    }


    fun getTotalDue(tenant: TenantItem): Double =
        tenant.totalDue ?: 0.0

    fun getNextDueLabel(tenant: TenantItem): String {
        val cycle = tenant.dueCycles?.firstOrNull() ?: return "—"

        return when {
            cycle.isOverdue -> "Overdue"
            cycle.isDueSoon -> "${daysUntil(cycle.dueDate)}d left"
            else -> "Due ${formatShortDate(cycle.dueDate)}"
        }
    }

    fun shouldAlertDueSoon(tenant: TenantItem): Boolean =
        tenant.hasDueSoon ?: (tenant.dueCycles?.firstOrNull()?.isDueSoon ?: false)

    private fun daysUntil(dueDateStr: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val due = sdf.parse(dueDateStr) ?: return 0
            val today = java.util.Date()
            val diff = due.time - today.time
            val days = diff / (1000 * 60 * 60 * 24)
            if (days > 0) days else 0
        } catch (e: Exception) {
            0
        }
    }

    private fun formatShortDate(dateStr: String): String {
        return try {
            val input = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val output = java.text.SimpleDateFormat("d MMM", java.util.Locale.getDefault())
            val date = input.parse(dateStr)
            if (date != null) output.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    fun isOverdue(tenant: TenantItem): Boolean =
        tenant.hasOverdue ?: false

    fun hasMultipleMonthsDue(tenant: TenantItem): Boolean =
        (tenant.dueMonthsCount ?: 0) > 1

    fun getDueCyclesSorted(tenant: TenantItem) =
        tenant.dueCycles.orEmpty().sortedBy { it.cycleMonth }

    fun getAllCyclesSorted(tenant: TenantItem) =
        tenant.allCycles.orEmpty().sortedBy { it.cycleMonth }

    // ─────────── FILTER HELPERS (tab switching) ───────────

    fun getTotalDuesAcrossAllTenants(): Double =
        allTenants.value
            .filter { (it.totalDue ?: 0.0) > 0.0 }
            .sumOf { it.totalDue ?: 0.0 }

    fun getOverdueTenants(): List<TenantItem> =
        allTenants.value.filter {

            it.hasOverdue == true &&
                    (it.totalDue ?: 0.0) > 0.0

        }

    fun getNoDueTenants(): List<TenantItem> =
        allTenants.value.filter { (it.totalDue ?: 0.0) <= 0.0 }

    fun getAllDueTenants(): List<TenantItem> =
        allTenants.value.filter { (it.totalDue ?: 0.0) > 0.0 }
}