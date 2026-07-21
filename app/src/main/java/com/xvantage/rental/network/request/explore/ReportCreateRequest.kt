package com.xvantage.rental.network.request.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches report.validation.js -> reportCreate
 */
data class ReportCreateRequest(

    @SerializedName("listing_fk")
    val listingFk: String,

    @SerializedName("reason")
    val reason: String, // fake_listing | wrong_info | already_rented | spam | other

    @SerializedName("description")
    val description: String? = null
)