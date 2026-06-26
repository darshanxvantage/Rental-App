package com.xvantage.rental.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeStats(
    val totalProperties: Int = 0,
    val activeTenants: Int = 0,
    val totalPayments: Double = 0.0,  // actual collected (amount field)
    val totalDues: Double = 0.0       // accurate from billing_cycles
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val homeStats = MutableStateFlow(HomeStats())
    val isLoading = MutableStateFlow(false)

    fun loadHomeStats() {
        viewModelScope.launch {
            isLoading.value = true

            // ── Properties count ──
            var propertyCount = 0
            when (val res = repository.getPropertyList()) {
                is ResultWrapper.Success -> {
                    propertyCount = res.value.data.rows.size
                }
                else -> {}
            }

            // ── Tenant stats from getTenantList (for active count + collected) ──
            var activeTenants = 0
            var totalPayments = 0.0

            when (val res = repository.getTenantList()) {
                is ResultWrapper.Success -> {
                    val rows = res.value.data.rows
                    activeTenants = rows.count {
                        it.status.equals("ACTIVE", ignoreCase = true)
                    }
                    // Total collected = sum of amount field
                    totalPayments = rows.sumOf {
                        it.amount?.toDoubleOrNull() ?: 0.0
                    }
                }
                else -> {}
            }

            // ── Total dues from getTenantDues (billing_cycles — accurate) ──
            // This is the SAME source as Due Payments screen
            // so both screens will always show the same number
            var totalDues = 0.0
            when (val res = repository.getTenantDues()) {
                is ResultWrapper.Success -> {
                    totalDues = res.value.data.tenants.sumOf {
                        it.totalDue ?: 0.0
                    }
                }
                else -> {}
            }

            homeStats.value = HomeStats(
                totalProperties = propertyCount,
                activeTenants   = activeTenants,
                totalPayments   = totalPayments,
                totalDues       = totalDues
            )

            isLoading.value = false
        }
    }
}