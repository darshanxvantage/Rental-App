package com.xvantage.rental.network.response.explore

/**
 * Generic envelope matching the backend's response/index.js shape exactly:
 *   { status, success, message, data, err }
 *
 * Used for every explore-module endpoint, e.g.:
 *   Response<ExploreApiResponse<List<ExploreCategoryResponse>>>
 *   Response<ExploreApiResponse<ExploreListingResponse>>
 *   Response<ExploreApiResponse<ExplorePaginatedResponse<ExploreListingResponse>>>
 */
data class ExploreApiResponse<T>(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: T?,
    val err: Any? = null
)