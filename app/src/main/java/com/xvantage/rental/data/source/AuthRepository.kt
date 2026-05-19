package com.xvantage.rental.data.source

import com.google.gson.JsonObject
import com.xvantage.rental.data.remote.APIInterface
import com.xvantage.rental.network.utils.NetworkHelper
import com.xvantage.rental.network.utils.ResultWrapper
import com.xvantage.rental.network.request.auth.LoginRequest
import com.xvantage.rental.network.request.auth.GoogleLoginRequest
import com.xvantage.rental.network.request.auth.SignupRequest
import com.xvantage.rental.network.request.auth.VerifyOTPRequest
import com.xvantage.rental.network.response.LoginResponse
import com.xvantage.rental.network.response.SignupResponse
import com.xvantage.rental.network.response.VerifyOTPResponse
import com.xvantage.rental.network.utils.ApiLogger
import javax.inject.Inject

class AuthRepository @Inject constructor(private val apiInterface: APIInterface) {

    suspend fun login(email: String, password: String): ResultWrapper<LoginResponse> {
        return try {
            val request = LoginRequest(email, password, "email")
            val response = apiInterface.login(request)
            ApiLogger.logRequest(request, response.raw().request)
            ApiLogger.logResponse(response, response.raw().request)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun signUp(email: String, password: String): ResultWrapper<SignupResponse> {
        return try {
            val request = SignupRequest(email, password)
            val response = apiInterface.signUp(request)
            ApiLogger.logRequest(request, response.raw().request)
            ApiLogger.logResponse(response, response.raw().request)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun signUpWithGoogle(googleData: GoogleLoginRequest): ResultWrapper<LoginResponse> {
        return try {
            val response = apiInterface.googleLogin(googleData)
            ApiLogger.logRequest(googleData, response.raw().request)
            ApiLogger.logResponse(response, response.raw().request)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun verifyOtp(email: String, otp: String, type: String): ResultWrapper<VerifyOTPResponse> {
        return try {
            val request = VerifyOTPRequest(email, otp, type)
            val response = apiInterface.verifyOtp(request)
            ApiLogger.logRequest(request, response.raw().request)
            ApiLogger.logResponse(response, response.raw().request)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun forgotPassword(email: String): ResultWrapper<JsonObject> {
        val params = mapOf("email" to email)
        val retrofitRequest = apiInterface.post("forgotPassword", params).request()

        return try {
            ApiLogger.logRequest(params, retrofitRequest)
            val response = apiInterface.post("forgotPassword", params).execute()
            ApiLogger.logResponse(response, retrofitRequest)
            if (response.isSuccessful) {
                ResultWrapper.Success(response.body() ?: JsonObject())
            } else {
                ResultWrapper.Error(
                    message = "Password reset failed: ${response.message()}",
                    statusCode = response.code(),
                    errorBody = response.errorBody()?.string()
                )
            }
        } catch (e: Exception) {
            ApiLogger.logError(e, retrofitRequest)
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }
}