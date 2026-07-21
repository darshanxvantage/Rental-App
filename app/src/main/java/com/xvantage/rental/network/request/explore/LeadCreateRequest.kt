package com.xvantage.rental.network.request.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches lead.validation.js -> leadCreate.
 * Fired the instant the student taps "Call Now" / "WhatsApp" - fire-and-forget,
 * never blocks opening the dialer/WhatsApp intent.
 */
data class LeadCreateRequest(

    @SerializedName("listing_fk")
    val listingFk: String,

    @SerializedName("lead_type")
    val leadType: String, // call | whatsapp | enquiry_form

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("phone_number")
    val phoneNumber: String? = null
)