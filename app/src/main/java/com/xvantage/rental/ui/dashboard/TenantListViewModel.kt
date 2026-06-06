package com.xvantage.rental.ui.dashboard

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
class TenantListViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val tenantList =
        MutableStateFlow<List<TenantItem>>(emptyList())

    fun loadTenants() {

        viewModelScope.launch {

            when (
                val response =
                    repository.getTenantList()
            ) {

                is ResultWrapper.Success -> {

                    tenantList.value =
                        response.value.data.rows
                }

                else -> {}
            }
        }
    }
}