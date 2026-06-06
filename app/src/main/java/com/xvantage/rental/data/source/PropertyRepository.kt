package com.xvantage.rental.data.source

import android.net.Uri
import android.provider.MediaStore
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
import com.xvantage.rental.utils.BaseApplication
import jakarta.inject.Inject
import okhttp3.MediaType
import com.xvantage.rental.network.response.PropertyListResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import com.xvantage.rental.network.response.TenantListResponse

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

                // Create a file from the URI
                val file = File(request.imageUri.path ?: "")
                val imageRequestBody = RequestBody.create(contentType.toMediaTypeOrNull(), file)

                // Create the MultipartBody.Part
                imagePart = MultipartBody.Part.createFormData(
                    "propertyImage",
                    file.name,
                    imageRequestBody
                )
            }

            // Create a request info map for logging
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
                imagePart
            )

            // Log response
            ApiLogger.logResponse(response, null)

            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ApiLogger.logError(e, null)
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getPropertyTypes(): ResultWrapper<List<PropertyType>> {
        return try {
            val response = apiInterface.get("room-type/property-type")
            val wrapper = NetworkHelper.handleApiResponse(response)
            when (wrapper) {
                is ResultWrapper.Success -> {
                    val jsonObject = wrapper.value
                    val dataArray = jsonObject.getAsJsonArray("data")
                    val listType = object : TypeToken<List<PropertyType>>() {}.type
                    val list: List<PropertyType> = Gson().fromJson(dataArray, listType)
                    ResultWrapper.Success(list)
                }

                is ResultWrapper.Error -> wrapper
                else -> ResultWrapper.Error("Unexpected response type")
            }
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
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
    suspend fun getTenantList():
            ResultWrapper<TenantListResponse> {

        return try {

            val response =
                apiInterface.getTenantList()

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }
}