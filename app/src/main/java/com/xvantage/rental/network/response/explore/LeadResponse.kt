package com.xvantage.rental.network.response.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches POST /explore/student/lead/create response data:
 * { contact_number, whatsapp_number } - returned so the app can immediately
 * launch the dialer/WhatsApp intent right after logging the lead.
 */
data class LeadResponse(

    @SerializedName("contact_number")
    val contactNumber: String,

    @SerializedName("whatsapp_number")
    val whatsappNumber: String? = null
)