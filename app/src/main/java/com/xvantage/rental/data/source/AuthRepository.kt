package com.xvantage.rental.data.source


import com.xvantage.rental.data.remote.APIInterface
import com.xvantage.rental.network.utils.NetworkHelper
import com.xvantage.rental.network.utils.ResultWrapper
import com.xvantage.rental.network.request.auth.LoginRequest
import com.xvantage.rental.utils.DeviceUtils
import com.xvantage.rental.network.request.auth.SignupRequest
import com.xvantage.rental.network.request.auth.VerifyOTPRequest
import com.xvantage.rental.network.response.LoginResponse
import com.xvantage.rental.network.response.SignupResponse
import com.xvantage.rental.network.response.VerifyOTPResponse
import com.xvantage.rental.network.utils.ApiLogger
import javax.inject.Inject

class AuthRepository @Inject constructor(private val apiInterface: APIInterface) {


    suspend fun login(
        phone: String
    ): ResultWrapper<LoginResponse> {

        return try {
            val request =
                LoginRequest(

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

    suspend fun signUp(
        phone: String
    ): ResultWrapper<SignupResponse> {

        return try {



            val request =
                SignupRequest(

                    phoneNumber = phone,


                    deviceType = "android",

                    deviceName =
                        DeviceUtils.getDeviceName(),

                    androidVersion =
                        DeviceUtils.getAndroidVersion()
                )



            val response =
                apiInterface.signUp(request)

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

            val request =
                VerifyOTPRequest(
                    phoneNumber = phone,
                    otp = otp
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


}