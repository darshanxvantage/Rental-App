package com.xvantage.rental.network.request.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches favorite.validation.js -> favoriteToggle
 */
data class FavoriteToggleRequest(

    @SerializedName("listing_fk")
    val listingFk: String
)