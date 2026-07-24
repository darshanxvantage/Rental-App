package com.xvantage.rental.network.response.explore

/**
 * Convenience, non-generic alias for the most common paginated response
 * shape used across the Discover/My-Listings screens:
 *
 *   Response<ExploreApiResponse<ExploreListingListResponse>>
 *
 * is exactly the same as:
 *
 *   Response<ExploreApiResponse<ExplorePaginatedResponse<ExploreListingResponse>>>
 *
 * Kept as its own typealias so call-sites and ViewModels can use a short,
 * readable name instead of repeating the generic every time.
 */
typealias ExploreListingListResponse = ExplorePaginatedResponse<ExploreListingResponse>