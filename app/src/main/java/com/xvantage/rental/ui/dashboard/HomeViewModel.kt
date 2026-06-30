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

    val totalDues: Double = 0.0,

    val monthlyRent: Double = 0.0,

    val monthPending: Double = 0.0,

    val lifetimeRevenue: Double = 0.0
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

            when (val res = repository.getDashboard()) {

                is ResultWrapper.Success -> {

                    val dashboard = res.value.data

                    homeStats.value = HomeStats(

                        totalProperties = dashboard.totalProperties,

                        activeTenants = dashboard.totalTenants,

                        totalPayments = dashboard.totalRentCollected,

                        totalDues = dashboard.totalRentDue,

                        monthlyRent = dashboard.monthlyRentTotal,

                        monthPending = dashboard.monthPending,

                        lifetimeRevenue = dashboard.lifetimeRevenue

                    )
                }

                is ResultWrapper.Error -> {

                    // Optional: log error
                }

                else -> {}

            }

            isLoading.value = false
        }
    }
}