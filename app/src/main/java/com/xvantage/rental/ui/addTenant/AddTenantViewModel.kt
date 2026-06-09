package com.xvantage.rental.ui.addTenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.response.TenantDetailsResponse
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.xvantage.rental.network.request.tenant.UpdateTenantRequest
import javax.inject.Inject

@HiltViewModel
class AddTenantViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val tenantDetails =
        MutableStateFlow<TenantDetailsResponse?>(null)

    val updateTenantState =
        MutableStateFlow(false)

    val createTenantState =
        MutableStateFlow(false)

    fun loadTenantDetails(
        tenantId: String
    ) {

        viewModelScope.launch {

            when (

                val response =
                    repository.getTenantDetails(
                        tenantId
                    )

            ) {

                is ResultWrapper.Success -> {

                    tenantDetails.value =
                        response.value
                }

                else -> {}
            }
        }
    }


    fun updateTenant(
        request: UpdateTenantRequest
    ) {

        viewModelScope.launch {

            when (

                repository.updateTenant(
                    request
                )

            ) {

                is ResultWrapper.Success -> {

                    updateTenantState.value =
                        true
                }

                else -> {

                    updateTenantState.value =
                        false
                }
            }
        }
    }
}