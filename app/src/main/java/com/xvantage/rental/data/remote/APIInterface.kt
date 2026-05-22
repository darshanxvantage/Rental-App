package com.xvantage.rental.data.remote

import com.google.gson.JsonObject
import com.xvantage.rental.network.request.auth.LoginRequest

import com.xvantage.rental.network.request.auth.SignupRequest
import com.xvantage.rental.network.request.auth.VerifyOTPRequest
import com.xvantage.rental.network.request.property.CreatePropertyRequest
import com.xvantage.rental.network.response.CreatePropertyResponse
import com.xvantage.rental.network.response.LoginResponse
import com.xvantage.rental.network.response.PropertyDetailsResponse
import com.xvantage.rental.network.response.SignupResponse
import com.xvantage.rental.network.response.VerifyOTPResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

import retrofit2.Response
import retrofit2.http.Body


import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface APIInterface {
    @GET
    suspend fun get(@Url url: String): Response<JsonObject>

    @POST("auth/sign-up")
    suspend fun signUp(@Body request: SignupRequest): Response<SignupResponse>

    @POST("auth/verify")
    suspend fun verifyOtp(@Body request: VerifyOTPRequest): Response<VerifyOTPResponse>


    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest): Response<LoginResponse>

    @GET
    suspend fun getProperty(@Url url: String): Response<PropertyDetailsResponse>
    @Multipart
    @POST("landlord/property/create")
    suspend fun createProperty(
        @Part("address") address: RequestBody,
        @Part("noOfRoom") noOfRoom: RequestBody,
        @Part("propertyTypeId") propertyTypeId: RequestBody,
        @Part("wa_number") wa_number: RequestBody,
        @Part("name") name: RequestBody,
        @Part propertyImage: MultipartBody.Part?
    ): Response<CreatePropertyResponse>
}
