package com.xvantage.rental.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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

            val currentMonth = SimpleDateFormat(
                "yyyy-MM", Locale.getDefault()
            ).format(Date())

            when (val res = repository.getDashboard(currentMonth)) {
                is ResultWrapper.Success -> {
                    val data = res.value.data
                    homeStats.value = HomeStats(
                        totalProperties = data.totalProperties,
                        activeTenants   = data.totalTenants,
                        totalPayments   = data.monthCollected,
                        totalDues       = data.totalRentDue
                    )
                }
                else -> {}
            }

            isLoading.value = false
        }
    }
}