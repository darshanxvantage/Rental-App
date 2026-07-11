package com.xvantage.rental.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.xvantage.rental.ui.dashboard.TenantListViewModel
import javax.inject.Inject

@HiltViewModel
class TenantListViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val tenantList =
        MutableStateFlow<List<TenantItem>>(emptyList())

    val tenantListError =
        MutableStateFlow<String?>(null)

    fun loadTenants() {

        viewModelScope.launch {

            when (
                val response =
                    repository.getTenantList()
            ) {

                is ResultWrapper.Success -> {

                    tenantListError.value = null

                    tenantList.value =
                        response.value.data.rows
                }

                is ResultWrapper.Error -> {

                    android.util.Log.e(
                        "TENANT_LIST_ERROR",
                        "Failed to load tenant list. statusCode=${response.statusCode}, message=${response.message}"
                    )

                    tenantListError.value = response.message
                }

                else -> {}
            }
        }
    }
}