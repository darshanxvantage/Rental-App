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

    val tenantError =
        MutableStateFlow<String?>(null)

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

                    tenantError.value = null

                    if (response.value.data == null) {

                        android.util.Log.e(
                            "TENANT_DETAILS_ERROR",
                            "Tenant $id: server returned success but data=null (tenant not found)"
                        )

                        tenantError.value =
                            "Could not load this tenant's details. It may have been deleted."

                    } else {

                        tenant.value =
                            response.value
                    }
                }

                is ResultWrapper.Error -> {

                    android.util.Log.e(
                        "TENANT_DETAILS_ERROR",
                        "Failed to load tenant $id. statusCode=${response.statusCode}, message=${response.message}"
                    )

                    tenantError.value = response.message
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