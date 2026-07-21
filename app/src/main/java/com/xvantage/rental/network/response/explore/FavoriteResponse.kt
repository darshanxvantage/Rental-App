package com.xvantage.rental.network.response.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches explore_favorites table row, joined with its listing
 * (used on GET /explore/student/favorite/my-list)
 */
data class FavoriteEntryResponse(

    @SerializedName("id")
    val id: String,

    @SerializedName("user_fk")
    val userFk: String,

    @SerializedName("listing_fk")
    val listingFk: String,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("listing")
    val listing: ExploreListingResponse? = null
)

/**
 * Matches POST /explore/student/favorite/toggle response data: { is_favorited }
 */
data class FavoriteToggleResponse(

    @SerializedName("is_favorited")
    val isFavorited: Boolean
)