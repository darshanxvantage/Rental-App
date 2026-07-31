package com.xvantage.rental.data.source

import android.net.Uri
import com.xvantage.rental.data.remote.ExploreApiInterface
import com.xvantage.rental.network.request.explore.CreateListingRequest
import com.xvantage.rental.network.request.explore.FavoriteToggleRequest
import com.xvantage.rental.network.request.explore.LeadCreateRequest
import com.xvantage.rental.network.request.explore.ReportCreateRequest
import com.xvantage.rental.network.request.explore.SharingPriceItem
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
import com.xvantage.rental.network.utils.NetworkHelper
import com.xvantage.rental.network.utils.ResultWrapper
import jakarta.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Explore feature repository - same shape as PropertyRepository:
 * every call is wrapped in try/catch -> NetworkHelper.handleApiResponse()
 * -> ResultWrapper<T>, so ViewModels consume it exactly like every other
 * repository in the app.
 */
class ExploreRepository @Inject constructor(
    private val apiInterface: ExploreApiInterface
) {

    // =====================================================================
    // COMMON
    // =====================================================================

    suspend fun getCategoryList(): ResultWrapper<ExploreApiResponse<List<ExploreCategoryResponse>>> {
        return try {
            val response = apiInterface.getCategoryList()
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getCategoryFields(
        categoryId: String
    ): ResultWrapper<ExploreApiResponse<ExploreCategoryFieldsWrapperResponse>> {
        return try {
            val response = apiInterface.getCategoryFields(categoryId)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun discoverList(
        currentPage: Int = 1,
        pageSize: Int = 10,
        city: String? = null,
        categoryFk: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        genderPreference: String? = null,
        foodIncluded: Boolean? = null,
        sharingType: String? = null,
        occupancyFor: String? = null,
        sortBy: String? = null,
        search: String? = null
    ): ResultWrapper<ExploreApiResponse<ExplorePaginatedResponse<ExploreListingResponse>>> {
        return try {
            val response = apiInterface.discoverList(
                currentPage, pageSize, city, categoryFk, minPrice, maxPrice,
                genderPreference, foodIncluded, sharingType, occupancyFor, sortBy, search
            )
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun discoverDetails(
        listingId: String
    ): ResultWrapper<ExploreApiResponse<ExploreListingResponse>> {
        return try {
            val response = apiInterface.discoverDetails(listingId)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun discoverNearby(
        latitude: Double,
        longitude: Double,
        radiusKm: Double? = null,
        currentPage: Int = 1,
        pageSize: Int = 10
    ): ResultWrapper<ExploreApiResponse<List<ExploreListingResponse>>> {
        return try {
            val response = apiInterface.discoverNearby(latitude, longitude, radiusKm, currentPage, pageSize)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun discoverSimilar(
        listingId: String
    ): ResultWrapper<ExploreApiResponse<List<ExploreListingResponse>>> {
        return try {
            val response = apiInterface.discoverSimilar(listingId)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    // =====================================================================
    // LANDLORD
    // =====================================================================

    suspend fun createListing(
        request: CreateListingRequest
    ): ResultWrapper<ExploreApiResponse<ExploreListingResponse>> {
        return try {
            val response = apiInterface.createListing(request)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun editListing(
        listingId: String,
        request: UpdateListingRequest
    ): ResultWrapper<ExploreApiResponse<ExploreListingResponse>> {
        return try {
            val response = apiInterface.editListing(listingId, request)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun myListings(
        currentPage: Int = 1,
        pageSize: Int = 10,
        status: String? = null
    ): ResultWrapper<ExploreApiResponse<ExplorePaginatedResponse<ExploreListingResponse>>> {
        return try {
            val response = apiInterface.myListings(currentPage, pageSize, status)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun myListingDetails(
        listingId: String
    ): ResultWrapper<ExploreApiResponse<ExploreListingResponse>> {
        return try {
            val response = apiInterface.myListingDetails(listingId)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun deleteListing(listingId: String): ResultWrapper<ExploreApiResponse<Any>> {
        return try {
            val response = apiInterface.deleteListing(listingId)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun saveSharingPrice(
        listingId: String,
        sharingPrices: List<SharingPriceItem>
    ): ResultWrapper<ExploreApiResponse<List<SharingPriceResponse>>> {
        return try {
            val response = apiInterface.saveSharingPrice(listingId, SharingPriceListRequest(sharingPrices))
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    /**
     * Converts locally-picked image Uris into Multipart parts and uploads them.
     * `type` matches explore_listing_images.type: cover | gallery | room | washroom | kitchen | document
     */
    suspend fun uploadListingImages(
        listingId: String,
        imageUris: List<Uri>,
        type: String? = null
    ): ResultWrapper<ExploreApiResponse<List<ExploreImageResponse>>> {
        return try {
            val imageParts = imageUris.mapNotNull { uri -> uriToMultipartPart(uri, "images") }
            val typePart: RequestBody? = type?.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = apiInterface.uploadListingImages(listingId, typePart, imageParts)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun deleteListingImage(imageId: String): ResultWrapper<ExploreApiResponse<Any>> {
        return try {
            val response = apiInterface.deleteListingImage(imageId)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    // owner's own KYC photo - separate from documents_required (tenant checklist)
    suspend fun uploadOwnerIdProof(
        listingId: String,
        idProofUri: Uri
    ): ResultWrapper<ExploreApiResponse<Map<String, String>>> {
        return try {
            val part = uriToMultipartPart(idProofUri, "owner_id_proof")
                ?: return ResultWrapper.Error("Could not read the selected ID proof photo")
            val response = apiInterface.uploadOwnerIdProof(listingId, part)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    // =====================================================================
    // STUDENT
    // =====================================================================

    suspend fun toggleFavorite(
        listingId: String
    ): ResultWrapper<ExploreApiResponse<FavoriteToggleResponse>> {
        return try {
            val response = apiInterface.toggleFavorite(FavoriteToggleRequest(listingId))
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun myFavorites(
        currentPage: Int = 1,
        pageSize: Int = 10
    ): ResultWrapper<ExploreApiResponse<ExplorePaginatedResponse<FavoriteEntryResponse>>> {
        return try {
            val response = apiInterface.myFavorites(currentPage, pageSize)
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun createLead(
        listingId: String,
        leadType: String,
        name: String? = null,
        phoneNumber: String? = null
    ): ResultWrapper<ExploreApiResponse<LeadResponse>> {
        return try {
            val response = apiInterface.createLead(
                LeadCreateRequest(listingFk = listingId, leadType = leadType, name = name, phoneNumber = phoneNumber)
            )
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun createReport(
        listingId: String,
        reason: String,
        description: String? = null
    ): ResultWrapper<ExploreApiResponse<Any>> {
        return try {
            val response = apiInterface.createReport(
                ReportCreateRequest(listingFk = listingId, reason = reason, description = description)
            )
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }

    private fun uriToMultipartPart(uri: Uri, partName: String): MultipartBody.Part? {
        val path = uri.path ?: return null
        val file = File(path)
        if (!file.exists()) return null

        val contentType = when {
            file.name.endsWith(".png", true) -> "image/png"
            file.name.endsWith(".webp", true) -> "image/webp"
            else -> "image/jpeg"
        }
        val requestBody = file.asRequestBody(contentType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestBody)
    }
    suspend fun myTenants(): ResultWrapper<ExploreApiResponse<List<com.xvantage.rental.network.response.explore.TenantResponse>>> {
        return try {
            val response = apiInterface.myTenants()
            NetworkHelper.handleApiResponse(response)
        } catch (e: Exception) {
            ResultWrapper.Error("Network error: ${e.localizedMessage}")
        }
    }
}

private fun File.asRequestBody(contentType: okhttp3.MediaType?): RequestBody {
    return RequestBody.create(contentType, this)
}