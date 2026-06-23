package com.xvantage.rental.ui.invoiceHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xvantage.rental.data.source.PropertyRepository
import com.xvantage.rental.network.response.InvoiceHistoryEntry
import com.xvantage.rental.network.utils.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceHistoryViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val invoices = MutableStateFlow<List<InvoiceHistoryEntry>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val errorMsg = MutableStateFlow<String?>(null)

    // tenantId is optional — pass it to show only one tenant's
    // invoices (e.g. opened from a specific tenant's room card),
    // or leave null to show every invoice across all properties.
    fun loadInvoices(tenantId: String? = null) {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = repository.getInvoiceHistory(tenantId)) {
                is ResultWrapper.Success -> {
                    invoices.value = result.value.data.invoices
                }
                is ResultWrapper.Error -> {
                    errorMsg.value = result.message
                }
                else -> {}
            }
            isLoading.value = false
        }
    }
}