package com.xvantage.rental.data.remote

import com.xvantage.rental.network.request.explore.CreateListingRequest
import com.xvantage.rental.network.request.explore.FavoriteToggleRequest
import com.xvantage.rental.network.request.explore.LeadCreateRequest
import com.xvantage.rental.network.request.explore.ReportCreateRequest
import com.xvantage.rental.network.request.explore.SharingPriceListRequest
import com.xvantage.rental.network.request.explore.UpdateListingRequest
import com.xvantage.rental.network.response.explore.ExploreApiResponse
import com.xvantage.rental.network.response.explore.ExploreCategoryFieldsWrapperResponse
import com.xvantage.rental.network.response.explore.ExploreCategoryResponse
import com.xvantage.rental.network.response.explore.ExploreImageResponse
import com.xvantage.rental.network.response.explore.ExploreListingResponse
import com.xvantage.rental.network.response.explore.ExplorePaginatedResponse
import com.xvantage.rental.network.response.explore.FavoriteEntryResponse
import com.xvantage.rental.network.response.explore.FavoriteToggleResponse
import com.xvantage.rental.network.response.explore.LeadResponse
import com.xvantage.rental.network.response.explore.SharingPriceResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ExploreApiInterface {



    @GET("explore/category/list")
    suspend fun getCategoryList(): Response<ExploreApiResponse<List<ExploreCategoryResponse>>>

    @GET("explore/category/{id}/fields")
    suspend fun getCategoryFields(
        @Path("id") categoryId: String
    ): Response<ExploreApiResponse<ExploreCategoryFieldsWrapperResponse>>

    @GET("explore/discover/list")
    suspend fun discoverList(
        @Query("currentPage") currentPage: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("city") city: String? = null,
        @Query("category_fk") categoryFk: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("gender_preference") genderPreference: String? = null,
        @Query("food_included") foodIncluded: Boolean? = null,
        @Query("sharing_type") sharingType: String? = null,
        @Query("occupancy_for") occupancyFor: String? = null,
        @Query("sort_by") sortBy: String? = null
    ): Response<ExploreApiResponse<ExplorePaginatedResponse<ExploreListingResponse>>>

    @GET("explore/discover/details/{id}")
    suspend fun discoverDetails(
        @Path("id") listingId: String
    ): Response<ExploreApiResponse<ExploreListingResponse>>

    @GET("explore/discover/nearby")
    suspend fun discoverNearby(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius_km") radiusKm: Double? = null,
        @Query("currentPage") currentPage: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<ExploreApiResponse<List<ExploreListingResponse>>>

    @GET("explore/discover/similar/{id}")
    suspend fun discoverSimilar(
        @Path("id") listingId: String
    ): Response<ExploreApiResponse<List<ExploreListingResponse>>>

    // =====================================================================
    // LANDLORD (owner-side listing management)
    // =====================================================================

    @POST("explore/landlord/listing/create")
    suspend fun createListing(
        @Body request: CreateListingRequest
    ): Response<ExploreApiResponse<ExploreListingResponse>>

    @PUT("explore/landlord/listing/edit/{id}")
    suspend fun editListing(
        @Path("id") listingId: String,
        @Body request: UpdateListingRequest
    ): Response<ExploreApiResponse<ExploreListingResponse>>

    @GET("explore/landlord/listing/my-list")
    suspend fun myListings(
        @Query("currentPage") currentPage: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("status") status: String? = null
    ): Response<ExploreApiResponse<ExplorePaginatedResponse<ExploreListingResponse>>>

    @GET("explore/landlord/listing/details/{id}")
    suspend fun myListingDetails(
        @Path("id") listingId: String
    ): Response<ExploreApiResponse<ExploreListingResponse>>

    @DELETE("explore/landlord/listing/{id}")
    suspend fun deleteListing(
        @Path("id") listingId: String
    ): Response<ExploreApiResponse<Any>>

    @POST("explore/landlord/listing/sharing-price/{id}")
    suspend fun saveSharingPrice(
        @Path("id") listingId: String,
        @Body request: SharingPriceListRequest
    ): Response<ExploreApiResponse<List<SharingPriceResponse>>>

    @Multipart
    @POST("explore/landlord/listing/images/{id}")
    suspend fun uploadListingImages(
        @Path("id") listingId: String,
        @Part("type") type: RequestBody?,
        @Part images: List<MultipartBody.Part>
    ): Response<ExploreApiResponse<List<ExploreImageResponse>>>

    @DELETE("explore/landlord/listing/images/{imageId}")
    suspend fun deleteListingImage(
        @Path("imageId") imageId: String
    ): Response<ExploreApiResponse<Any>>

    // owner's own KYC photo - separate from documents_required (tenant checklist)
    @Multipart
    @POST("explore/landlord/listing/aadhar/{id}")
    suspend fun uploadOwnerAadhar(
        @Path("id") listingId: String,
        @Part ownerAadhar: MultipartBody.Part
    ): Response<ExploreApiResponse<Map<String, String>>>

    // =====================================================================
    // STUDENT (discover-side consumer actions)
    // =====================================================================

    @POST("explore/student/favorite/toggle")
    suspend fun toggleFavorite(
        @Body request: FavoriteToggleRequest
    ): Response<ExploreApiResponse<FavoriteToggleResponse>>

    @GET("explore/student/favorite/my-list")
    suspend fun myFavorites(
        @Query("currentPage") currentPage: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<ExploreApiResponse<ExplorePaginatedResponse<FavoriteEntryResponse>>>

    @POST("explore/student/lead/create")
    suspend fun createLead(
        @Body request: LeadCreateRequest
    ): Response<ExploreApiResponse<LeadResponse>>

    @POST("explore/student/report/create")
    suspend fun createReport(
        @Body request: ReportCreateRequest
    ): Response<ExploreApiResponse<Any>>
}