package com.xvantage.rental.data.source

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xvantage.rental.data.remote.APIInterface
import com.xvantage.rental.network.request.property.CreatePropertyRequest
import com.xvantage.rental.network.response.CreatePropertyResponse
import com.xvantage.rental.network.response.PropertyDetailsResponse
import com.xvantage.rental.network.response.PropertyType
import com.xvantage.rental.network.utils.ApiLogger
import com.xvantage.rental.network.utils.NetworkHelper
import com.xvantage.rental.network.utils.ResultWrapper
import com.xvantage.rental.network.response.DashboardResponse
import com.xvantage.rental.network.request.feedback.FeedbackRequest
import com.xvantage.rental.network.response.FeedbackResponse
import jakarta.inject.Inject
import com.xvantage.rental.network.response.TenantDetailsResponse
import com.xvantage.rental.network.response.PaymentSuccessResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import com.xvantage.rental.network.request.tenant.TenantPaymentRequest
import com.xvantage.rental.network.response.PropertyListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.xvantage.rental.network.request.tenant.StatusRequest
import java.io.File
import com.xvantage.rental.network.response.TenantListResponse
import com.xvantage.rental.network.response.TenantDuesResponse
import com.xvantage.rental.network.response.InvoiceHistoryResponse
import com.xvantage.rental.network.response.StatementResponse
import com.google.gson.JsonObject
import com.xvantage.rental.network.request.property.UpdatePropertyRequest
import com.xvantage.rental.network.request.tenant.UpdateTenantRequest

/**
 * Project: Rental App By XV Team
 * Author: Mujammil x Vipul x XV Team
 * Date:  06/05/25
 * <p>
 * Licensed under the Apache License, Version 2.0. See LICENSE file for terms.
 */

class PropertyRepository @Inject constructor(private val apiInterface: APIInterface) {

    suspend fun createProperty(request: CreatePropertyRequest): ResultWrapper<CreatePropertyResponse> {
        return try {
            // Convert string fields to RequestBody objects
            val addressPart = RequestBody.create("text/plain".toMediaTypeOrNull(), request.address)
            val noOfRoomPart = RequestBody.create("text/plain".toMediaTypeOrNull(), request.noOfRoom.toString())
            val propertyTypeIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), request.propertyTypeId)
            val waNumberPart = RequestBody.create("text/plain".toMediaTypeOrNull(), request.wa_number)
            val namePart = RequestBody.create("text/plain".toMediaTypeOrNull(), request.name)

            fun optionalPart(value: String?): RequestBody? =
                value?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }

            val cityPart = optionalPart(request.city)
            val statePart = optionalPart(request.state)
            val pincodePart = optionalPart(request.pincode)
            val rentModePart = optionalPart(request.rentMode)
            val floorConfigPart = optionalPart(request.floorConfig)
            val maintenanceChargePart = optionalPart(request.maintenanceCharge)
            val unitNumbersPart = if (request.unitNumbers.isNotEmpty()) optionalPart(Gson().toJson(request.unitNumbers)) else null
            val sharingTypePart = optionalPart(request.sharingType)
            val bedCountPart = request.bedCount?.let { optionalPart(it.toString()) }

            // Handle image part
            var imagePart: MultipartBody.Part? = null

            if (request.imageUri != null) {
                // Get content type from Uri
                val contentType = request.imageUri.lastPathSegment?.let {
                    when {
                        it.endsWith(".jpg", true) || it.endsWith(".jpeg", true) -> "image/jpeg"
                        it.endsWith(".png", true) -> "image/png"
                        else -> "image/*"
                    }
                } ?: "image/*"


                val file = File(request.imageUri.path ?: "")
                val imageRequestBody = RequestBody.create(contentType.toMediaTypeOrNull(), file)


                imagePart = MultipartBody.Part.createFormData(
                    "propertyImage",
                    file.name,
                    imageRequestBody
                )
            }

            val requestInfo = HashMap<String, Any>()
            requestInfo["address"] = request.address
            requestInfo["noOfRoom"] = request.noOfRoom
            requestInfo["propertyTypeId"] = request.propertyTypeId
            requestInfo["wa_number"] = request.wa_number
            requestInfo["name"] = request.name
            requestInfo["hasImage"] = (request.imageUri != null)

            // Log request with our simple map
            ApiLogger.logRequest(requestInfo, null)

            // Make the API call
            val response = apiInterface.createProperty(
                addressPart,
                noOfRoomPart,
                propertyTypeIdPart,
                waNumberPart,
                namePart,
                imagePart,
                cityPart,
                statePart,
                pincodePart,
                rentModePart,
                floorConfigPart,
                maintenanceChargePart,
                unitNumbersPart,
                sharingTypePart,
                bedCountPart
            )

            // Log response
            ApiLogger.logResponse(response, null)

            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ApiLogger.logError(e, null)
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }




    suspend fun updateProperty(
        request: UpdatePropertyRequest
    ): ResultWrapper<CreatePropertyResponse> {

        return try {

            val propertyIdPart =
                RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    request.propertyId
                )

            val addressPart =
                RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    request.address
                )

            val noOfRoomPart =
                RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    request.noOfRoom.toString()
                )

            val propertyTypeIdPart =
                RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    request.propertyTypeId
                )

            val waNumberPart =
                RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    request.wa_number
                )

            val namePart =
                RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    request.name
                )

            var imagePart: MultipartBody.Part? = null

            if (request.imageUri != null) {

                val file =
                    File(
                        request.imageUri.path ?: ""
                    )

                val requestFile =
                    RequestBody.create(
                        "image/*".toMediaTypeOrNull(),
                        file
                    )

                imagePart =
                    MultipartBody.Part.createFormData(
                        "propertyImage",
                        file.name,
                        requestFile
                    )
            }

            fun optionalPart(value: String?): RequestBody? =
                value?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }

            val response =
                apiInterface.updateProperty(

                    propertyIdPart,

                    addressPart,

                    noOfRoomPart,

                    propertyTypeIdPart,

                    waNumberPart,

                    namePart,

                    imagePart,

                    optionalPart(request.city),

                    optionalPart(request.state),

                    optionalPart(request.pincode),

                    optionalPart(request.rentMode),

                    optionalPart(request.floorConfig),

                    optionalPart(request.maintenanceCharge),

                    if (request.unitNumbers.isNotEmpty()) optionalPart(Gson().toJson(request.unitNumbers)) else null,

                    optionalPart(request.sharingType),

                    request.bedCount?.let { optionalPart(it.toString()) }
                )

            NetworkHelper.handleApiResponse(
                response
            )

        } catch (e: Exception) {

            ResultWrapper.Error(
                e.localizedMessage ?: "Update failed"
            )
        }
    }



    suspend fun deleteProperty(
        propertyId: String
    ): ResultWrapper<JsonObject> {

        return try {

            val response =
                apiInterface.deleteProperty(
                    propertyId
                )

            NetworkHelper.handleApiResponse(
                response
            )

        } catch (e: Exception) {

            ResultWrapper.Error(
                e.localizedMessage ?: "Delete failed"
            )
        }
    }

    suspend fun getPropertyTypes(): ResultWrapper<List<PropertyType>> {

        return try {

            val response =
                apiInterface.get(
                    "room-type/property-type"
                )


            android.util.Log.d(
                "PROPERTY_TYPE_API",
                "Request URL = ${response.raw().request.url}"
            )

            android.util.Log.d(
                "PROPERTY_TYPE_API",
                "HTTP Code = ${response.code()}"
            )

            android.util.Log.d(
                "PROPERTY_TYPE_API",
                "Response Body = ${response.body()}"
            )


            val wrapper =
                NetworkHelper.handleApiResponse(response)


            when (wrapper) {

                is ResultWrapper.Success -> {

                    val jsonObject = wrapper.value

                    if (!jsonObject.has("data")) {

                        return ResultWrapper.Error(
                            "Response does not contain data field"
                        )
                    }


                    val dataArray =
                        jsonObject.getAsJsonArray("data")


                    val listType =
                        object :
                            TypeToken<List<PropertyType>>() {}.type


                    val list: List<PropertyType> =
                        Gson().fromJson(
                            dataArray,
                            listType
                        )


                    android.util.Log.d(
                        "PROPERTY_TYPE_API",
                        "Parsed Property Types = $list"
                    )


                    ResultWrapper.Success(list)
                }


                is ResultWrapper.Error -> {

                    android.util.Log.e(
                        "PROPERTY_TYPE_API",
                        "API Error = ${wrapper.message}"
                    )

                    wrapper
                }


                else -> {

                    ResultWrapper.Error(
                        "Unexpected response type"
                    )
                }
            }


        } catch (e: Exception) {

            android.util.Log.e(
                "PROPERTY_TYPE_API",
                "Exception",
                e
            )

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }
    suspend fun getPropertyDetails(id: String): ResultWrapper<PropertyDetailsResponse> {
        return try {

            val response =
                apiInterface.getProperty(
                    "landlord/property/details/$id"
                )

            android.util.Log.e(
                "PROPERTY_DETAILS_URL",
                response.raw().request.url.toString()
            )

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            android.util.Log.e(
                "PROPERTY_DETAILS_ERROR",
                e.toString()
            )

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }
    suspend fun getPropertyList():
            ResultWrapper<PropertyListResponse> {

        return try {

            val response =
                apiInterface.getPropertyList()

            android.util.Log.e(
                "PROPERTY_URL",
                response.raw().request.url.toString()
            )

            android.util.Log.e(
                "PROPERTY_CODE",
                response.code().toString()
            )

            android.util.Log.e(
                "PROPERTY_BODY",
                response.body().toString()
            )

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            android.util.Log.e(
                "PROPERTY_EXCEPTION",
                e.toString()
            )

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }
    suspend fun getTenantList(): ResultWrapper<TenantListResponse> {

        return try {

            val response = apiInterface.getTenantList()

            android.util.Log.e(
                "TENANT_API_URL",
                response.raw().request.url.toString()
            )

            android.util.Log.e(
                "TENANT_API_CODE",
                response.code().toString()
            )

            android.util.Log.e(
                "TENANT_API_BODY",
                response.body().toString()
            )

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }

    // Backed by the tenant_billing_cycles table — returns each
    // tenant along with a month-wise breakdown (dueCycles) of every
    // pending/partial month, instead of a single accumulated number.
    suspend fun getTenantDues(): ResultWrapper<TenantDuesResponse> {

        return try {

            val response = apiInterface.getTenantDues()

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }

    suspend fun getDashboard(
        month: String? = null
    ): ResultWrapper<DashboardResponse> {

        return try {

            val response = apiInterface.getDashboard(month)

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }

    suspend fun getInvoiceHistory(
        tenantId: String? = null
    ): ResultWrapper<InvoiceHistoryResponse> {

        return try {

            val response = apiInterface.getInvoiceHistory(tenantId)

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }

    suspend fun generateCompleteStatement(
        tenantId: String
    ): ResultWrapper<StatementResponse> {

        return try {

            val response = apiInterface.generateCompleteStatement(tenantId)

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }

    suspend fun getTenantDetails(
        id: String
    ): ResultWrapper<TenantDetailsResponse> {

        return try {

            val response =
                apiInterface.getTenantDetails(id)

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            ResultWrapper.Error(
                e.localizedMessage ?: "Error"
            )
        }
    }




    suspend fun updateTenantStatus(
        tenantId: String,
        status: String
    ): ResultWrapper<JsonObject> {

        return try {

            val response =
                apiInterface.updateTenantStatus(
                    tenantId,
                    StatusRequest(status)
                )

            NetworkHelper.handleApiResponse(
                response
            )

        } catch (e: Exception) {

            ResultWrapper.Error(
                e.localizedMessage ?: "Status Update Failed"
            )
        }
    }

    suspend fun receivePayment(

        request: TenantPaymentRequest

    ): ResultWrapper<PaymentSuccessResponse> {

        return try {

            val response =
                apiInterface.tenantPayment(request)

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            ResultWrapper.Error(
                e.localizedMessage ?: "Payment Failed"
            )
        }
    }

    suspend fun createTenant(

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

        leaseType: String,

        leaseEndDate: String,

        profilePic: MultipartBody.Part?,

        documents: List<MultipartBody.Part>?

    ): ResultWrapper<JsonObject> {

        return try {

            val response = apiInterface.createTenant(

                roomId.toRequestBody("text/plain".toMediaTypeOrNull()),

                tenantName.toRequestBody("text/plain".toMediaTypeOrNull()),

                phoneNumber.toRequestBody("text/plain".toMediaTypeOrNull()),

                phoneCode.toRequestBody("text/plain".toMediaTypeOrNull()),

                rent.toRequestBody("text/plain".toMediaTypeOrNull()),

                roomDeposit.toRequestBody("text/plain".toMediaTypeOrNull()),

                checkinDate.toRequestBody("text/plain".toMediaTypeOrNull()),

                rentStartDate.toRequestBody("text/plain".toMediaTypeOrNull()),

                rentSubmissionDate.toRequestBody("text/plain".toMediaTypeOrNull()),

                fixedWaterBill.toRequestBody("text/plain".toMediaTypeOrNull()),

                fixedElectricity.toRequestBody("text/plain".toMediaTypeOrNull()),

                fixedWaterBillAmount.toRequestBody("text/plain".toMediaTypeOrNull()),

                fixedElectricityAmount.toRequestBody("text/plain".toMediaTypeOrNull()),

                costPerUnit.toRequestBody("text/plain".toMediaTypeOrNull()),

                meterReading.toRequestBody("text/plain".toMediaTypeOrNull()),

                meterReadingWater.toRequestBody("text/plain".toMediaTypeOrNull()),

                costUnitWater.toRequestBody("text/plain".toMediaTypeOrNull()),

                referenceName.toRequestBody("text/plain".toMediaTypeOrNull()),

                leaseType.toRequestBody("text/plain".toMediaTypeOrNull()),

                leaseEndDate.toRequestBody("text/plain".toMediaTypeOrNull()),

                profilePic,

                documents

            )

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            ResultWrapper.Error(e.localizedMessage ?: "Tenant Create Failed")

        }
    }

    suspend fun updateTenant(
        request: UpdateTenantRequest
    ): ResultWrapper<JsonObject> {

        return try {

            val tenantIdPart =
                request.tenantId.toRequestBody("text/plain".toMediaTypeOrNull())

            val tenantNamePart =
                request.tenantName.toRequestBody("text/plain".toMediaTypeOrNull())

            val phoneNumberPart =
                request.phoneNumber.toRequestBody("text/plain".toMediaTypeOrNull())

            val phoneCodePart =
                request.phoneCode.toRequestBody("text/plain".toMediaTypeOrNull())

            val rentPart =
                request.rent.toRequestBody("text/plain".toMediaTypeOrNull())

            val roomDepositPart =
                request.roomDeposit.toRequestBody("text/plain".toMediaTypeOrNull())

            val rentStartDatePart =
                request.rentStartDate.toRequestBody("text/plain".toMediaTypeOrNull())

            val rentSubmissionDatePart =
                request.rentSubmissionDate.toRequestBody("text/plain".toMediaTypeOrNull())

            val leaseTypePart =
                request.leaseType.toRequestBody("text/plain".toMediaTypeOrNull())

            val leaseEndDatePart =
                request.leaseEndDate.toRequestBody("text/plain".toMediaTypeOrNull())

            val fixedWaterBillAmountPart =
                request.fixedWaterBillAmount.toRequestBody("text/plain".toMediaTypeOrNull())

            val fixedElectricityAmountPart =
                request.fixedElectricityAmount.toRequestBody("text/plain".toMediaTypeOrNull())

            val meterReadingPart =
                request.meterReading.toRequestBody("text/plain".toMediaTypeOrNull())

            val waterReadingPart =
                request.waterReading.toRequestBody("text/plain".toMediaTypeOrNull())

            val costPerUnitPart =
                request.costPerUnit.toRequestBody("text/plain".toMediaTypeOrNull())

            val costUnitWaterPart =
                request.costUnitWater.toRequestBody("text/plain".toMediaTypeOrNull())

            // The request already contains properly-created multipart parts.
            // Do not treat them as Uri objects or attempt to build them again.
            val profilePicPart = request.profilePic
            val documentParts = request.documents


            val response = apiInterface.updateTenant(

                tenantIdPart,

                tenantNamePart,

                phoneNumberPart,

                phoneCodePart,

                rentPart,

                roomDepositPart,

                rentStartDatePart,

                rentSubmissionDatePart,

                fixedWaterBillAmountPart,

                fixedElectricityAmountPart,

                meterReadingPart,

                waterReadingPart,

                costPerUnitPart,

                costUnitWaterPart,

                leaseTypePart,

                leaseEndDatePart,

                profilePicPart,

                documentParts?.takeIf { it.isNotEmpty() }

            )

            return NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            return ResultWrapper.Error(
                e.localizedMessage ?: "Tenant Update Failed"
            )

        }
    }

    suspend fun createRoom(

        propertyId: String,

        propertyTypeId: String,

        roomNo: String,

        roomTypeId: String,

        roomTypeText: String,

        address: String,

        rent: String,

        meterReading: String,

        meterReadingLastDate: String,

        roomImage: MultipartBody.Part?,

        // PG: sharing type + bed count. Row House: which floor this unit is.
        // Left null for property types that don't use them.
        sharingType: String? = null,

        bedCount: String? = null,

        floorLabel: String? = null

    ): ResultWrapper<JsonObject> {

        return try {

            val response = apiInterface.createRoom(

                propertyId.toRequestBody("text/plain".toMediaTypeOrNull()),

                propertyTypeId.toRequestBody("text/plain".toMediaTypeOrNull()),

                roomNo.toRequestBody("text/plain".toMediaTypeOrNull()),

                roomTypeId.toRequestBody("text/plain".toMediaTypeOrNull()),

                roomTypeText.toRequestBody("text/plain".toMediaTypeOrNull()),

                address.toRequestBody("text/plain".toMediaTypeOrNull()),

                rent.toRequestBody("text/plain".toMediaTypeOrNull()),

                meterReading.toRequestBody("text/plain".toMediaTypeOrNull()),

                meterReadingLastDate.toRequestBody("text/plain".toMediaTypeOrNull()),

                roomImage,

                sharingType?.toRequestBody("text/plain".toMediaTypeOrNull()),

                bedCount?.toRequestBody("text/plain".toMediaTypeOrNull()),

                floorLabel?.toRequestBody("text/plain".toMediaTypeOrNull())

            )

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            ResultWrapper.Error(
                e.localizedMessage ?: "Room Create Failed"
            )

        }
    }






    suspend fun deleteRoom(roomId: String): ResultWrapper<Boolean> {
        return try {
            val response = apiInterface.deleteRoom(roomId)
            if (response.isSuccessful) ResultWrapper.Success(true)
            else ResultWrapper.Error("Delete room failed")
        } catch (e: Exception) {
            ResultWrapper.Error(e.localizedMessage ?: "Delete room failed")
        }
    }

    suspend fun editRoom(
        roomId: String,
        roomNo: String,
        rent: String
    ): ResultWrapper<Boolean> {
        return try {
            val response = apiInterface.editRoom(
                roomId.toRequestBody("text/plain".toMediaTypeOrNull()),
                roomNo.toRequestBody("text/plain".toMediaTypeOrNull()),
                rent.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            if (response.isSuccessful) ResultWrapper.Success(true)
            else ResultWrapper.Error("Edit room failed")
        } catch (e: Exception) {
            ResultWrapper.Error(e.localizedMessage ?: "Edit room failed")
        }
    }

    suspend fun deleteTenant(
        tenantId: String
    ): ResultWrapper<Boolean> {

        return try {

            val response =
                apiInterface.deleteTenant(
                    tenantId
                )

            if (response.isSuccessful) {

                ResultWrapper.Success(
                    true
                )

            } else {

                ResultWrapper.Error(
                    "Delete Failed"
                )
            }

        } catch (e: Exception) {

            ResultWrapper.Error(
                e.localizedMessage ?: "Error"
            )
        }
    }


    suspend fun deleteTenantPermanent(
        tenantId: String
    ): ResultWrapper<Boolean> {

        return try {

            val response =
                apiInterface.deleteTenantPermanent(
                    tenantId
                )

            if (response.isSuccessful) {

                ResultWrapper.Success(
                    true
                )

            } else {

                ResultWrapper.Error(
                    "Delete Failed"
                )
            }

        } catch (e: Exception) {

            ResultWrapper.Error(
                e.localizedMessage ?: "Error"
            )
        }
    }
    suspend fun submitFeedback(request: FeedbackRequest): ResultWrapper<FeedbackResponse> {
        return try {
            val response = apiInterface.submitFeedback(request)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }
}
