
package com.xvantage.rental.data.remote

import com.google.gson.JsonObject
import com.xvantage.rental.network.request.auth.CreateProfileRequest
import com.xvantage.rental.network.request.auth.LoginRequest
import com.xvantage.rental.network.request.auth.SignupRequest
import com.xvantage.rental.network.request.auth.VerifyOTPRequest
import com.xvantage.rental.network.response.CreateProfileResponse
import com.xvantage.rental.network.response.CreatePropertyResponse
import com.xvantage.rental.network.response.LoginResponse
import com.xvantage.rental.network.response.PropertyDetailsResponse
import com.xvantage.rental.network.response.SignupResponse
import com.xvantage.rental.network.response.VerifyOTPResponse
import com.xvantage.rental.network.response.PropertyListResponse
import com.xvantage.rental.network.response.TenantListResponse
import retrofit2.http.Path
import com.xvantage.rental.network.response.TenantDetailsResponse
import retrofit2.http.DELETE
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url
import retrofit2.http.PUT
import retrofit2.http.Query

interface   APIInterface {

    @GET
    suspend fun get(
        @Url url: String
    ): Response<JsonObject>



    @POST("auth/sign-up")
    suspend fun signUp(
        @Body request: SignupRequest
    ): Response<SignupResponse>


    @POST("auth/verify")
    suspend fun verifyOtp(
        @Body request: VerifyOTPRequest
    ): Response<VerifyOTPResponse>


    @POST("landlord/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>


    @POST("landlord/auth/verify-login")
    suspend fun verifyLoginOtp(
        @Body request: VerifyOTPRequest
    ): Response<VerifyOTPResponse>


    @POST("auth/create-profile")
    suspend fun createProfile(
        @Body request: CreateProfileRequest
    ): Response<CreateProfileResponse>



    @GET
    suspend fun getProperty(
        @Url url: String
    ): Response<PropertyDetailsResponse>

    @GET("landlord/property/list")
    suspend fun getPropertyList(
        @Query("currentPage") page: Int = 1,
        @Query("pageSize") size: Int = 100
    ): Response<PropertyListResponse>

    @GET("landlord/tenant/list")
    suspend fun getTenantList(
        @Query("currentPage") page: Int = 1,
        @Query("pageSize") size: Int = 100
    ): Response<TenantListResponse>

    @GET("landlord/tenant/{id}")
    suspend fun getTenantDetails(
        @Path("id") id: String
    ): Response<TenantDetailsResponse>


    @Multipart
    @POST("landlord/property/create")
    suspend fun createProperty(

        @Part("address")
        address: RequestBody,

        @Part("noOfRoom")
        noOfRoom: RequestBody,

        @Part("propertyTypeId")
        propertyTypeId: RequestBody,

        @Part("wa_number")
        wa_number: RequestBody,

        @Part("name")
        name: RequestBody,

        @Part
        propertyImage: MultipartBody.Part?

    ): Response<CreatePropertyResponse>


    @Multipart
    @PUT("landlord/property/edit")
    suspend fun updateProperty(

        @Part("propertyId")
        propertyId: RequestBody,

        @Part("address")
        address: RequestBody,

        @Part("noOfRoom")
        noOfRoom: RequestBody,

        @Part("propertyTypeId")
        propertyTypeId: RequestBody,

        @Part("wa_number")
        waNumber: RequestBody,

        @Part("name")
        name: RequestBody,

        @Part
        propertyImage: MultipartBody.Part?

    ): Response<CreatePropertyResponse>


    @DELETE("landlord/property/{propertyId}")
    suspend fun deleteProperty(

        @Path("propertyId")
        propertyId: String

    ): Response<JsonObject>


    @Multipart
    @PUT("auth/profile")
    suspend fun updateProfileImage(

        @Part("first_name")
        firstName: RequestBody,

        @Part
        profilePic: MultipartBody.Part?

    ): Response<CreateProfileResponse>
}

