package com.xvantage.rental.network.request.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches listing.validation.js -> sharingPriceItemSchema
 */
data class SharingPriceItem(

    @SerializedName("sharing_type")
    val sharingType: String, // single | double | triple | four_sharing | dormitory

    @SerializedName("price")
    val price: Double,

    @SerializedName("security_deposit")
    val securityDeposit: Double? = null,

    @SerializedName("maintenance_charge")
    val maintenanceCharge: Double? = null,

    @SerializedName("available_beds")
    val availableBeds: Int
)