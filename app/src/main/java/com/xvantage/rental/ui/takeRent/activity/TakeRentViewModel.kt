package com.xvantage.rental.ui.takeRent.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.response.PropertyItem
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TakeRentViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val propertyList = MutableStateFlow<List<PropertyItem>>(emptyList())
    val tenantList   = MutableStateFlow<List<TenantItem>>(emptyList())
    val isLoading    = MutableStateFlow(false)
    val errorMsg     = MutableStateFlow<String?>(null)

    fun loadData() {
        viewModelScope.launch {
            isLoading.value = true


            val propertyDeferred = async { repository.getPropertyList() }
            val tenantDeferred   = async { repository.getTenantList() }

            val propertyResult = propertyDeferred.await()
            val tenantResult   = tenantDeferred.await()

            when (propertyResult) {
                is ResultWrapper.Success -> {
                    propertyList.value = propertyResult.value.data.rows
                    android.util.Log.d("TakeRent", "Properties loaded: ${propertyList.value.size}")
                }
                else -> {
                    errorMsg.value = "Failed to load properties"
                    android.util.Log.e("TakeRent", "Property load failed: $propertyResult")
                }
            }

            when (tenantResult) {
                is ResultWrapper.Success -> {
                    tenantList.value = tenantResult.value.data.rows
                    android.util.Log.d("TakeRent", "Tenants loaded: ${tenantList.value.size}")
                    tenantList.value.forEach { t ->
                        android.util.Log.d("TakeRent",
                            "Tenant: ${t.tenant_name} | property_fk: ${t.property_fk} | room: ${t.tenant_details?.room_no} | status: ${t.status}")
                    }
                }
                else -> {
                    errorMsg.value = "Failed to load tenants"
                    android.util.Log.e("TakeRent", "Tenant load failed: $tenantResult")
                }
            }

            isLoading.value = false
        }
    }
}