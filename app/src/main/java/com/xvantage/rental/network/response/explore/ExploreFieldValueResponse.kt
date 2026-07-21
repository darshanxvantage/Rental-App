package com.xvantage.rental.network.response.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches explore_listing_field_values table (joined with its field definition
 * for display - so the UI can show the label + type without a second lookup).
 */
data class ExploreFieldValueResponse(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("listing_fk")
    val listingFk: String? = null,

    @SerializedName("category_field_fk")
    val categoryFieldFk: String,

    @SerializedName("value")
    val value: String? = null,

    @SerializedName("field")
    val field: ExploreCategoryFieldResponse? = null
)