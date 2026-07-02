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
import com.xvantage.rental.network.response.PropertyItem
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import android.util.Log
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

    val propertyListState =
        MutableStateFlow<List<PropertyItem>>(emptyList())

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

    fun loadPropertyList() {

        viewModelScope.launch {

            when (

                val response =
                    repository.getPropertyList()

            ) {

                is ResultWrapper.Success -> {

                    propertyListState.value =
                        response.value.data.rows

                }

                else -> {

                }
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


    fun createTenant(

        roomId: String,

        tenantName: String,

        phoneNumber: String,

        phoneCode: String,

        rent: String,

        roomDeposit: String,

        checkinDate: String,

        rentStartDate: String,

        rentSubmissionDate: String,

        fixedWaterBill: String,

        fixedElectricity: String,

        fixedWaterBillAmount: String,

        fixedElectricityAmount: String,

        costPerUnit: String,

        meterReading: String,

        meterReadingWater: String,

        costUnitWater: String,

        referenceName: String,

        profilePic: MultipartBody.Part?,

        documents: List<MultipartBody.Part>?

    ) {

        viewModelScope.launch {

            when (val response = repository.createTenant(

                roomId,
                tenantName,
                phoneNumber,
                phoneCode,
                rent,
                roomDeposit,
                checkinDate,
                rentStartDate,
                rentSubmissionDate,
                fixedWaterBill,
                fixedElectricity,
                fixedWaterBillAmount,
                fixedElectricityAmount,
                costPerUnit,
                meterReading,
                meterReadingWater,
                costUnitWater,
                referenceName,
                profilePic,
                documents

            )) {

                is ResultWrapper.Success -> {

                    Log.e(
                        "CREATE_TENANT",
                        "SUCCESS"
                    )

                    createTenantState.value = true
                }

                is ResultWrapper.Error -> {

                    Log.e(
                        "CREATE_TENANT",
                        "ERROR = ${response.message}"
                    )

                    createTenantState.value = false
                }

                else -> {

                    Log.e(
                        "CREATE_TENANT",
                        "UNKNOWN ERROR"
                    )

                    createTenantState.value = false
                }
            }
        }
    }
}