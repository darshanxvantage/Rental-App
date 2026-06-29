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

    fun getElectricityInfo(tenant: TenantItem): Pair<String, String> {
        val mode = tenant.fixed_electricity?.trim()?.lowercase()
        val currentCycle = tenant.dueCycles?.firstOrNull()

        return when (mode) {
            "no cost", "no_cost", "none" -> Pair("No Cost", "₹0")
            "fix", "fixed" -> {
                val amt = currentCycle?.electricityAmount
                    ?: tenant.fixed_electricity_amount?.toDoubleOrNull()
                    ?: 0.0
                Pair("Fixed", "₹${amt.toLong()}")
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
                Pair("Fixed", "₹${amt.toLong()}")
            }
            "meter", "metered" -> Pair("Metered", "Per usage")
            else -> Pair("No Cost", "₹0")
        }
    }


    fun getTotalDue(tenant: TenantItem): Double =
        tenant.totalDue ?: 0.0

    fun getNextDueLabel(tenant: TenantItem): String =
        tenant.dueCycles?.firstOrNull()?.monthLabel ?: "—"

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
        allTenants.value.sumOf { it.totalDue ?: 0.0 }

    fun getOverdueTenants(): List<TenantItem> =
        allTenants.value.filter { it.hasOverdue == true }

    fun getNoDueTenants(): List<TenantItem> =
        allTenants.value.filter { (it.totalDue ?: 0.0) <= 0.0 }
}