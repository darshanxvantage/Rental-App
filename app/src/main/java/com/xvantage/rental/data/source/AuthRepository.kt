package com.xvantage.rental.data.source

import com.xvantage.rental.data.remote.APIInterface
import com.xvantage.rental.network.request.auth.CreateProfileRequest
import com.xvantage.rental.network.request.auth.LoginRequest
import com.xvantage.rental.network.request.auth.VerifyOTPRequest
import com.xvantage.rental.network.response.CreateProfileResponse
import com.xvantage.rental.network.response.LoginResponse
import com.xvantage.rental.network.response.VerifyOTPResponse
import com.xvantage.rental.network.utils.ApiLogger
import com.xvantage.rental.utils.DeviceUtils
import com.xvantage.rental.network.utils.NetworkHelper
import com.xvantage.rental.network.utils.ResultWrapper
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.util.Log

class AuthRepository @Inject constructor(
    private val apiInterface: APIInterface
) {


    suspend fun login(
        phone: String
    ): ResultWrapper<LoginResponse> {

        return try {


            val request = LoginRequest(

                phoneNumber = phone,

                deviceName =
                    DeviceUtils.getDeviceName(),

                androidVersion =
                    DeviceUtils.getAndroidVersion()
            )

            val response =
                apiInterface.login(request)

            ApiLogger.logRequest(
                request,
                response.raw().request
            )

            ApiLogger.logResponse(
                response,
                response.raw().request
            )

            NetworkHelper.handleApiResponse(
                response
            )

        } catch (e: Exception) {

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }


    suspend fun verifyOtp(
        phone: String,
        otp: String
    ): ResultWrapper<VerifyOTPResponse> {

        return try {

            val request = VerifyOTPRequest(
                phoneNumber = phone,
                otp = otp,
                deviceType = "android"
            )

            val response =
                apiInterface.verifyOtp(request)

            ApiLogger.logRequest(
                request,
                response.raw().request
            )

            ApiLogger.logResponse(
                response,
                response.raw().request
            )

            NetworkHelper.handleApiResponse(
                response
            )

        } catch (e: Exception) {

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }


    suspend fun verifyLoginOtp(
        phone: String,
        otp: String
    ): ResultWrapper<VerifyOTPResponse> {

        return try {

            val request = VerifyOTPRequest(
                phoneNumber = phone,
                otp = otp,
                deviceType = "android"
            )

            val response =
                apiInterface.verifyLoginOtp(request)

            ApiLogger.logRequest(
                request,
                response.raw().request
            )

            ApiLogger.logResponse(
                response,
                response.raw().request
            )

            NetworkHelper.handleApiResponse(
                response
            )

        } catch (e: Exception) {

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }
    }


    suspend fun createProfile(
        firstName: String,
        lastName: String,
        email: String,
        state: String,
        city: String,
        gender: String
    ): ResultWrapper<CreateProfileResponse> {

        return try {

            val request = CreateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                state = state,
                city = city,
                gender = gender
            )

            val response =
                apiInterface.createProfile(request)

            ApiLogger.logRequest(
                request,
                response.raw().request
            )

            ApiLogger.logResponse(
                response,
                response.raw().request
            )

            NetworkHelper.handleApiResponse(
                response
            )

        } catch (e: Exception) {

            ResultWrapper.Error(
                "Network error: ${e.localizedMessage}"
            )
        }

    }
    suspend fun updateProfileImage(
        firstName: String? = null,
        lastName: String? = null,
        imageFile: File? = null
    ): ResultWrapper<CreateProfileResponse> {

        return try {

            val firstNameBody =
                firstName?.takeIf { it.isNotBlank() }?.toRequestBody(
                    "text/plain".toMediaTypeOrNull()
                )

            val lastNameBody =
                lastName?.takeIf { it.isNotBlank() }?.toRequestBody(
                    "text/plain".toMediaTypeOrNull()
                )

            val imagePart =
                imageFile?.let { file ->

                    val requestFile =
                        file.asRequestBody(
                            "image/*".toMediaTypeOrNull()
                        )

                    MultipartBody.Part.createFormData(
                        "profile_pic",
                        file.name,
                        requestFile
                    )
                }

            val response =
                apiInterface.updateProfileImage(
                    firstNameBody,
                    lastNameBody,
                    imagePart
                )

            Log.e(
                "PROFILE_UPLOAD",
                "Code = ${response.code()}"
            )

            Log.e(
                "PROFILE_UPLOAD",
                "Body = ${response.body()}"
            )

            Log.e(
                "PROFILE_UPLOAD",
                "ErrorBody = ${response.errorBody()?.string()}"
            )

            NetworkHelper.handleApiResponse(response)

        } catch (e: Exception) {

            Log.e(
                "PROFILE_UPLOAD",
                "Error = ${e.localizedMessage}"
            )

            ResultWrapper.Error(
                "Upload failed: ${e.localizedMessage}"
            )
        }
    }
}