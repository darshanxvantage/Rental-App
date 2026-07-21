package com.xvantage.rental.network.response.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches explore_listing_images table.
 * `image` is just the filename - full URL is built by ExploreConstants.imageBaseUrl() + image.
 */
data class ExploreImageResponse(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("listing_fk")
    val listingFk: String? = null,

    @SerializedName("image")
    val image: String,

    @SerializedName("type")
    val type: String, // cover | gallery | room | washroom | kitchen | document

    @SerializedName("sort_order")
    val sortOrder: Int = 0
)