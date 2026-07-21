package com.xvantage.rental.network.request.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches POST /explore/landlord/listing/sharing-price/:id body shape:
 * { sharing_prices: [...] }
 */
data class SharingPriceListRequest(

    @SerializedName("sharing_prices")
    val sharingPrices: List<SharingPriceItem>
)