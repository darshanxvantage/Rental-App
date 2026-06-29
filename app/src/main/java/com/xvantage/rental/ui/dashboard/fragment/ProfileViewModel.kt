package com.xvantage.rental.ui.dashboard.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.xvantage.rental.network.utils.ResultWrapper

data class LandlordTier(
    val title: String,
    val colorHex: String,
    val progress: Int,
    val hint: String
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val propertyCount = MutableStateFlow(0)
    val tenantCount   = MutableStateFlow(0)


    val revenueTotal = MutableStateFlow(0.0)

    // Total collected = actual payments received (amount field)
    val collectedTotal = MutableStateFlow(0.0)

    // Total pending = billing_cycles se accurate dues
    val pendingTotal = MutableStateFlow(0.0)

    val landlordTier: StateFlow<LandlordTier> =
        propertyCount.map { count -> computeTier(count) }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                computeTier(0)
            )

    fun loadDashboardData() {
        viewModelScope.launch {


            when (val res = repository.getPropertyList()) {
                is ResultWrapper.Success ->
                    propertyCount.value = res.value.data.totalItems
                else -> {}
            }


            when (val res = repository.getTenantList()) {
                is ResultWrapper.Success -> {
                    val rows = res.value.data.rows
                    val active = rows.filter {
                        it.status?.uppercase() == "ACTIVE"
                    }
                    tenantCount.value = active.size

                    // Actual collected payments
                    collectedTotal.value = rows.sumOf {
                        it.amount?.toDoubleOrNull() ?: 0.0
                    }
                }
                else -> {}
            }

            when (val res = repository.getTenantDues()) {
                is ResultWrapper.Success -> {
                    val tenants = res.value.data.tenants


                    revenueTotal.value = tenants.sumOf { tenant ->
                        tenant.rent?.toDoubleOrNull() ?: 0.0
                    }

                    pendingTotal.value = tenants.sumOf {
                        it.totalDue ?: 0.0
                    }
                }
                else -> {}
            }
        }
    }

    private fun computeTier(propertyCount: Int): LandlordTier {
        return when {
            propertyCount <= 0 -> LandlordTier(
                title = "Starter Landlord",
                colorHex = "#64748B",
                progress = 0,
                hint = "Add your first property to start leveling up"
            )
            propertyCount < 3 -> LandlordTier(
                title = "Growing Landlord",
                colorHex = "#16A34A",
                progress = (propertyCount * 100) / 3,
                hint = "${3 - propertyCount} more to reach Pro Landlord"
            )
            propertyCount < 6 -> LandlordTier(
                title = "Pro Landlord",
                colorHex = "#F59E0B",
                progress = (propertyCount * 100) / 6,
                hint = "${6 - propertyCount} more to reach Elite Landlord"
            )
            else -> LandlordTier(
                title = "Elite Landlord",
                colorHex = "#7C3AED",
                progress = 100,
                hint = "You've reached the top tier 🏆"
            )
        }
    }
}