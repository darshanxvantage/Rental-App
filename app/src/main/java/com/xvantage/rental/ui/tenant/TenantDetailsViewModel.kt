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
}