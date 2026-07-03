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
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TakeRentViewModel @Inject constructor(
    private val repository: PropertyRepository
) : ViewModel() {

    val propertyList = MutableStateFlow<List<PropertyItem>>(emptyList())
    val tenantList   = MutableStateFlow<List<TenantItem>>(emptyList())
    val isLoading    = MutableStateFlow(false)
    val errorMsg     = MutableStateFlow<String?>(null)

    val statementFilePath      = MutableStateFlow<String?>(null)
    val isGeneratingStatement  = MutableStateFlow(false)

    fun generateCompleteStatement(tenantId: String) {
        viewModelScope.launch {
            isGeneratingStatement.value = true
            when (val result = repository.generateCompleteStatement(tenantId)) {
                is ResultWrapper.Success -> statementFilePath.value = result.value.data.filePath
                is ResultWrapper.Error   -> errorMsg.value = result.message
                else -> {}
            }
            isGeneratingStatement.value = false
        }
    }

    fun clearStatementFilePath() { statementFilePath.value = null }

    private fun getNextDueDateFromCycles(
        dueCycles: List<com.xvantage.rental.network.response.DueCycle>?
    ): String? {
        return dueCycles?.firstOrNull()?.dueDate
    }

    fun loadData() {
        viewModelScope.launch {
            isLoading.value = true

            val propertyDeferred = async { repository.getPropertyList() }
            val tenantDeferred   = async { repository.getTenantList() }
            val duesDeferred     = async { repository.getTenantDues() }

            val propertyResult = propertyDeferred.await()
            val tenantResult   = tenantDeferred.await()
            val duesResult     = duesDeferred.await()

            when (propertyResult) {
                is ResultWrapper.Success -> propertyList.value = propertyResult.value.data.rows
                else -> errorMsg.value = "Failed to load properties"
            }

            when (tenantResult) {
                is ResultWrapper.Success -> {
                    val tenants = tenantResult.value.data.rows.toMutableList()

                    if (duesResult is ResultWrapper.Success) {
                        val duesMap = duesResult.value.data.tenants.associateBy { it.id }

                        val merged = tenants.map { tenant ->
                            val dueData = duesMap[tenant.id]
                            if (dueData != null) {
                                tenant.copy(
                                    // Accurate due from billing_cycles
                                    payment_due = (dueData.totalDue ?: 0.0).toString(),

                                    rent_end_date = getNextDueDateFromCycles(dueData.dueCycles)
                                        ?: tenant.rent_end_date
                                )
                            } else {
                                tenant
                            }
                        }
                        tenantList.value = merged
                    } else {
                        tenantList.value = tenants
                    }
                }
                else -> errorMsg.value = "Failed to load tenants"
            }

            isLoading.value = false
        }
    }
}