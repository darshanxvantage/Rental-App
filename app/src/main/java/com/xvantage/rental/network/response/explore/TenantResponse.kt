package com.xvantage.rental.network.response.explore

import com.google.gson.annotations.SerializedName

/**
 * Matches the response of GET /explore/landlord/tenant/my-tenants
 * (explore-module/controller/v1/landlord/tenant.controller.js).
 * Pulls straight from the OLD/core rental-flow Tenant + Property tables -
 * this is how the Explore side's "Tenant" tab links back into the old flow.
 */
data class TenantResponse(

    @SerializedName("id")
    val id: String,

    @SerializedName("tenant_name")
    val tenantName: String? = null,

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("room_name")
    val roomName: String? = null,

    @SerializedName("rent")
    val rent: Double? = null,

    @SerializedName("profile_pic")
    val profilePic: String? = null,

    @SerializedName("checkin_date")
    val checkinDate: String? = null,

    @SerializedName("property_fk")
    val propertyFk: String? = null,

    @SerializedName("property_name")
    val propertyName: String? = null,

    @SerializedName("property_address")
    val propertyAddress: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null

    // due_amount / due_card fields intentionally left out for now -
    // to be designed and added in a later pass (per plan).
)