package com.xvantage.rental.network.response.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches explore_listing_sharing_price table.
 */
data class SharingPriceResponse(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("listing_fk")
    val listingFk: String? = null,

    @SerializedName("sharing_type")
    val sharingType: String, // single | double | triple | four_sharing | dormitory

    @SerializedName("price")
    val price: Double,

    @SerializedName("security_deposit")
    val securityDeposit: Double? = null,

    @SerializedName("maintenance_charge")
    val maintenanceCharge: Double? = null,

    @SerializedName("available_beds")
    val availableBeds: Int = 0,

    @SerializedName("status")
    val status: Boolean = true
)

/**
 * Matches ExploreUtil.getPriceRangeSummary() output: { min_price, max_price }
 * Attached to every listing card/detail so the UI can show "₹6,000 - ₹9,500".
 */
data class PriceRangeResponse(

    @SerializedName("min_price")
    val minPrice: Double? = null,

    @SerializedName("max_price")
    val maxPrice: Double? = null
)