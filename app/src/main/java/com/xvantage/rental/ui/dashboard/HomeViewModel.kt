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
    val totalPayments: Double = 0.0,   // sum of amount (collected)
    val totalDues: Double = 0.0        // sum of payment_due (pending)
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

            // ── Tenant stats ──
            var activeTenants = 0
            var totalPayments = 0.0
            var totalDues     = 0.0

            when (val res = repository.getTenantList()) {
                is ResultWrapper.Success -> {
                    val rows = res.value.data.rows

                    // Active tenants only
                    activeTenants = rows.count {
                        it.status.equals("ACTIVE", ignoreCase = true)
                    }

                    // Total payments = sum of `amount` field (actual collected)
                    totalPayments = rows.sumOf {
                        it.amount?.toDoubleOrNull() ?: 0.0
                    }

                    // Total dues = sum of `payment_due` field (pending)
                    totalDues = rows.sumOf {
                        it.payment_due?.toDoubleOrNull() ?: 0.0
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