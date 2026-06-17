package com.xvantage.rental.ui.dashboard.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DuesViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val allTenants   = MutableStateFlow<List<TenantItem>>(emptyList())
    val isLoading    = MutableStateFlow(false)
    val errorMsg     = MutableStateFlow<String?>(null)

    fun loadDues() {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = repository.getTenantList()) {
                is ResultWrapper.Success -> {
                    // Only show ACTIVE tenants
                    val active = result.value.data.rows.filter {
                        it.status.equals("ACTIVE", ignoreCase = true)
                    }
                    allTenants.value = active
                }
                is ResultWrapper.Error -> {
                    errorMsg.value = result.message
                }
                else -> {}
            }
            isLoading.value = false
        }
    }

    // Total outstanding dues across all active tenants
    fun getTotalDues(): Double {
        return allTenants.value.sumOf {
            it.payment_due?.toDoubleOrNull() ?: 0.0
        }
    }

    // Tenants who have payment_due > 0
    fun getOverdueTenants(): List<TenantItem> {
        return allTenants.value.filter {
            (it.payment_due?.toDoubleOrNull() ?: 0.0) > 0.0
        }
    }

    // Tenants with no dues (payment_due == 0 or null)
    fun getNoDueTenants(): List<TenantItem> {
        return allTenants.value.filter {
            (it.payment_due?.toDoubleOrNull() ?: 0.0) <= 0.0
        }
    }
}