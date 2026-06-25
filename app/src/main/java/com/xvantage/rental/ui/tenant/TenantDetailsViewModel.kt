package com.xvantage.rental.ui.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.response.TenantDetailsResponse
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TenantDetailsViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val tenant =
        MutableStateFlow<TenantDetailsResponse?>(null)

    val statusUpdateState =
        MutableStateFlow(false)

    val deleteState =
        MutableStateFlow(false)

    fun loadTenant(id: String) {

        viewModelScope.launch {

            when (
                val response =
                    repository.getTenantDetails(id)
            ) {

                is ResultWrapper.Success -> {

                    tenant.value =
                        response.value
                }

                else -> {}
            }
        }
    }
    fun deleteTenant(
        tenantId: String
    ) {

        viewModelScope.launch {

            when (
                repository.deleteTenant(
                    tenantId
                )
            ) {

                is ResultWrapper.Success -> {

                    deleteState.value = true

                }

                else -> {

                    deleteState.value = false

                }
            }
        }
    }

    fun deleteTenantPermanent(
        tenantId: String
    ) {

        viewModelScope.launch {

            when (
                repository.deleteTenantPermanent(
                    tenantId
                )
            ) {

                is ResultWrapper.Success -> {

                    deleteState.value = true

                }

                else -> {

                    deleteState.value = false

                }
            }
        }
    }

    fun updateTenantStatus(
        tenantId: String,
        status: String
    ) {

        viewModelScope.launch {

            when (

                repository.updateTenantStatus(
                    tenantId,
                    status
                )

            ) {

                is ResultWrapper.Success -> {

                    statusUpdateState.value =
                        true
                }

                else -> {

                    statusUpdateState.value =
                        false
                }
            }
        }
    }
}