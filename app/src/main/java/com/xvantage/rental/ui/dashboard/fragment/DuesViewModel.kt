package com.xvantage.rental.ui.dashboard.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DuesViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val allTenants = MutableStateFlow<List<TenantItem>>(emptyList())
    val isLoading  = MutableStateFlow(false)
    val errorMsg   = MutableStateFlow<String?>(null)

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun loadDues() {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = repository.getTenantList()) {
                is ResultWrapper.Success -> {
                    val active = result.value.data.rows.filter {
                        it.status.equals("ACTIVE", ignoreCase = true)
                    }
                    allTenants.value = active
                }
                is ResultWrapper.Error -> {
                    errorMsg.value = result.message
                }
                else -> {}
            }
            isLoading.value = false
        }
    }

    // ─────────── PAYMENT CYCLE LOGIC ───────────

    fun getNextDueDate(tenant: TenantItem): String {
        val startDate = tenant.rent_start_date ?: return "—"

        return try {
            val start    = sdf.parse(startDate) ?: return "—"
            val startCal = Calendar.getInstance().apply { time = start }
            val dueDay   = startCal.get(Calendar.DAY_OF_MONTH)

            val today    = Calendar.getInstance()

            // Last payment date check
            val lastPaid = tenant.rent_receive_date
            val lastPaidCal: Calendar? = if (!lastPaid.isNullOrEmpty()) {
                val parsed = sdf.parse(lastPaid)
                if (parsed != null) Calendar.getInstance().apply { time = parsed } else null
            } else null

            val nextDue = Calendar.getInstance()

            if (lastPaidCal != null) {
                // Agar last payment isi month ki hai → next due agle month same date
                nextDue.set(Calendar.YEAR,  lastPaidCal.get(Calendar.YEAR))
                nextDue.set(Calendar.MONTH, lastPaidCal.get(Calendar.MONTH) + 1)
                nextDue.set(Calendar.DAY_OF_MONTH, dueDay)
            } else {
                // No payment yet → current month me same day
                nextDue.set(Calendar.DAY_OF_MONTH, dueDay)
                // Agar wo date nikal gayi → next month
                if (nextDue.before(today)) {
                    nextDue.add(Calendar.MONTH, 1)
                }
            }

            sdf.format(nextDue.time)

        } catch (e: Exception) {
            "—"
        }
    }

    fun isOverdue(tenant: TenantItem): Boolean {
        val nextDueStr = getNextDueDate(tenant)
        if (nextDueStr == "—") return false

        return try {
            val nextDue = sdf.parse(nextDueStr) ?: return false
            val today   = Date()
            val due     = tenant.payment_due?.toDoubleOrNull() ?: 0.0
            nextDue.before(today) && due > 0
        } catch (e: Exception) {
            false
        }
    }

    // ─────────── ELECTRICITY CHARGE ───────────

    fun getElectricityInfo(tenant: TenantItem): Pair<String, String> {

        return when (tenant.fixed_electricity?.trim()?.lowercase()) {

            "no cost", "no_cost", "none" ->
                Pair("No Cost", "₹0")

            "fix", "fixed" -> {

                val amt =
                    tenant.fixed_electricity_amount?.toDoubleOrNull() ?: 0.0

                Pair("Fixed", "₹${amt.toLong()}")
            }

            "meter", "metered" -> {

                val current =
                    tenant.meter_reading?.toDoubleOrNull() ?: 0.0

                val previous =
                    tenant.last_meter_reading?.toDoubleOrNull() ?: 0.0

                val units =
                    (current - previous).coerceAtLeast(0.0)

                val rate =
                    tenant.cost_per_unit?.toDoubleOrNull() ?: 0.0

                Pair(
                    "Metered",
                    "₹${(units * rate).toLong()} (${units.toLong()} units)"
                )
            }

            else -> Pair("No Cost", "₹0")
        }
    }

    // ─────────── WATER CHARGE ───────────

    fun getWaterInfo(tenant: TenantItem): Pair<String, String> {

        return when (tenant.fixed_waterbill?.trim()?.lowercase()) {

            "no cost", "no_cost", "none" ->
                Pair("No Cost", "₹0")

            "fix", "fixed" -> {

                val amt =
                    tenant.fixed_waterbill_amount?.toDoubleOrNull() ?: 0.0

                Pair("Fixed", "₹${amt.toLong()}")
            }

            "meter", "metered" -> {

                val current =
                    tenant.meter_reading_water?.toDoubleOrNull() ?: 0.0

                val previous =
                    tenant.last_meter_reading_water?.toDoubleOrNull() ?: 0.0

                val units =
                    (current - previous).coerceAtLeast(0.0)

                val rate =
                    tenant.cost_unit_water?.toDoubleOrNull() ?: 0.0

                Pair(
                    "Metered",
                    "₹${(units * rate).toLong()} (${units.toLong()} units)"
                )
            }

            else -> Pair("No Cost", "₹0")
        }
    }

    // ─────────── TOTAL PAYABLE ───────────

    fun getTotalPayable(tenant: TenantItem): Long {
        val rent = tenant.rent?.toDoubleOrNull() ?: 0.0

        val elec = when (tenant.fixed_electricity?.lowercase()?.trim()) {
            "fix", "fixed"  -> tenant.fixed_electricity_amount?.toDoubleOrNull() ?: 0.0
            "meter", "metered"-> {
                val units = ((tenant.meter_reading?.toDoubleOrNull() ?: 0.0) -
                        (tenant.last_meter_reading?.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
                units * (tenant.cost_per_unit?.toDoubleOrNull() ?: 0.0)
            }
            else -> 0.0
        }

        val water = when (tenant.fixed_waterbill?.lowercase()?.trim()) {
            "fix", "fixed" -> tenant.fixed_waterbill_amount?.toDoubleOrNull() ?: 0.0
            "meter", "metered" -> {
                val units = ((tenant.meter_reading_water?.toDoubleOrNull() ?: 0.0) -
                        (tenant.last_meter_reading_water?.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
                units * (tenant.cost_unit_water?.toDoubleOrNull() ?: 0.0)
            }
            else -> 0.0
        }

        return (rent + elec + water).toLong()
    }

    // ─────────── FILTER HELPERS ───────────

    fun getTotalDues(): Double =
        allTenants.value.sumOf { it.payment_due?.toDoubleOrNull() ?: 0.0 }

    fun getOverdueTenants(): List<TenantItem> =
        allTenants.value.filter { isOverdue(it) }

    // No due = payment_due == 0
    fun getNoDueTenants(): List<TenantItem> =
        allTenants.value.filter {
            (it.payment_due?.toDoubleOrNull() ?: 0.0) <= 0.0
        }
}