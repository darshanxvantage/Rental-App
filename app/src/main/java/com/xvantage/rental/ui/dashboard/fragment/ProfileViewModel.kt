package com.xvantage.rental.ui.dashboard.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.xvantage.rental.network.utils.ResultWrapper

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val propertyCount = MutableStateFlow(0)

    val tenantCount = MutableStateFlow(0)

    fun loadDashboardData() {

        viewModelScope.launch {

            when (val propertyResponse =
                repository.getPropertyList()) {

                is ResultWrapper.Success -> {

                    propertyCount.value =
                        propertyResponse.value.data.totalItems
                }

                else -> {}
            }

            when (val tenantResponse =
                repository.getTenantList()) {

                is ResultWrapper.Success -> {

                    tenantCount.value =
                        tenantResponse.value.data.totalItems
                }

                else -> {}
            }
        }
    }
}