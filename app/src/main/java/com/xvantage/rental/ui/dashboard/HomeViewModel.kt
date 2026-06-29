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
    val totalPayments: Double = 0.0,
    val totalDues: Double = 0.0
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

            // ── Properties ──
            var propertyCount = 0
            when (val res = repository.getPropertyList()) {
                is ResultWrapper.Success ->
                    propertyCount = res.value.data.rows.size
                else -> {}
            }

            // ── Active tenants + collected payments ──
            var activeTenants  = 0
            var totalPayments  = 0.0
            when (val res = repository.getTenantList()) {
                is ResultWrapper.Success -> {
                    val rows = res.value.data.rows
                    activeTenants = rows.count {
                        it.status.equals("ACTIVE", ignoreCase = true)
                    }
                    totalPayments = rows.sumOf {
                        it.amount?.toDoubleOrNull() ?: 0.0
                    }
                }
                else -> {}
            }

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