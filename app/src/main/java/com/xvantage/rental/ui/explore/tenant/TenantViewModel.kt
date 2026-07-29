package com.xvantage.rental.ui.explore.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.ExploreRepository
import com.xvantage.rental.network.response.explore.TenantResponse
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TenantViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {

    val tenants = MutableStateFlow<List<TenantResponse>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    fun loadTenants() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            when (val result = repository.myTenants()) {
                is ResultWrapper.Success -> {
                    tenants.value = result.value.data ?: emptyList()
                }
                is ResultWrapper.Error -> {
                    errorMessage.value = result.message
                }
                else -> {}
            }

            isLoading.value = false
        }
    }
}