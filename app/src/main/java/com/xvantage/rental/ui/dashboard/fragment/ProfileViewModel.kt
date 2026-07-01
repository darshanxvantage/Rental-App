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
import java.text.SimpleDateFormat
import java.util.*
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

    val propertyCount  = MutableStateFlow(0)
    val tenantCount    = MutableStateFlow(0)

    // This month's total rent billed (prorated correctly)
    val revenueTotal      = MutableStateFlow(0.0)
    val monthlyRentTotal  = MutableStateFlow(0.0)  // same as revenueTotal

    // This month's collected payments
    val collectedTotal    = MutableStateFlow(0.0)

    // Outstanding pending dues (same as Due screen)
    val pendingTotal      = MutableStateFlow(0.0)

    // Collection rate percent
    val collectionRate    = MutableStateFlow(0)

    // All time collected since ever
    val lifetimeRevenue   = MutableStateFlow(0.0)

    val landlordTier: StateFlow<LandlordTier> =
        propertyCount.map { computeTier(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, computeTier(0))

    fun loadDashboardData() {
        viewModelScope.launch {

            val currentMonth = SimpleDateFormat(
                "yyyy-MM", Locale.getDefault()
            ).format(Date())


            when (val res = repository.getDashboard(currentMonth)) {
                is ResultWrapper.Success -> {
                    val d = res.value.data
                    propertyCount.value   = d.totalProperties
                    tenantCount.value     = d.totalTenants
                    revenueTotal.value    = d.monthlyRentTotal
                    monthlyRentTotal.value = d.monthlyRentTotal
                    collectedTotal.value  = d.monthCollected
                    pendingTotal.value    = d.totalRentDue
                    collectionRate.value  = d.collectionRatePercent
                    lifetimeRevenue.value = d.lifetimeRevenue
                }
                else -> {}
            }
        }
    }

    private fun computeTier(count: Int): LandlordTier = when {
        count <= 0 -> LandlordTier(
            "Starter Landlord", "#64748B", 0,
            "Add your first property to start leveling up"
        )
        count < 3 -> LandlordTier(
            "Growing Landlord", "#16A34A", (count * 100) / 3,
            "${3 - count} more to reach Pro Landlord"
        )
        count < 6 -> LandlordTier(
            "Pro Landlord", "#F59E0B", (count * 100) / 6,
            "${6 - count} more to reach Elite Landlord"
        )
        else -> LandlordTier(
            "Elite Landlord", "#7C3AED", 100,
            "You've reached the top tier 🏆"
        )
    }
}