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

    val tenantCount = MutableStateFlow(0)

    // Total monthly rent of all ACTIVE tenants (rent field)
    val revenueTotal = MutableStateFlow(0.0)

    // Total collected amount from all tenants (amount field = actual payments received)
    val collectedTotal = MutableStateFlow(0.0)

    // Total pending due from all tenants
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

            // Load property count
            when (val propertyResponse = repository.getPropertyList()) {

                is ResultWrapper.Success -> {
                    propertyCount.value = propertyResponse.value.data.totalItems
                }

                else -> {}
            }

            // Load tenant data and calculate revenue stats
            when (val tenantResponse = repository.getTenantList()) {

                is ResultWrapper.Success -> {

                    val rows = tenantResponse.value.data.rows

                    // Count only ACTIVE tenants
                    val activeTenants = rows.filter {
                        it.status?.uppercase() == "ACTIVE"
                    }

                    tenantCount.value = activeTenants.size

                    // Monthly rent total = sum of rent of all ACTIVE tenants
                    revenueTotal.value = activeTenants.sumOf {
                        it.rent?.toDoubleOrNull() ?: 0.0
                    }

                    // Total collected = sum of amount field (actual payments received)
                    collectedTotal.value = rows.sumOf {
                        it.amount?.toDoubleOrNull() ?: 0.0
                    }

                    // Total pending dues
                    pendingTotal.value = rows.sumOf {
                        it.payment_due?.toDoubleOrNull() ?: 0.0
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