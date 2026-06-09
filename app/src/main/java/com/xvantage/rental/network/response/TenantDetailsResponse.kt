package com.xvantage.rental.network.response

data class TenantDetailsResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: TenantDetailsData
)

data class TenantDetailsData(

    val id: String,

    val tenant_name: String,

    val refrence_name: String?,

    val phone_number: String?,

    val profile_pic: String?,

    val rent: String?,

    val status: String?,

    val room_deposit: String?,

    val fixed_electricity_amount: String?,

    val fixed_waterbill_amount: String?,

    val cost_per_unit: String?,

    val cost_unit_water: String?,

    val checkin_date: String?,

    val rent_start_date: String?,

    val note: String?,

    val documents: List<TenantDocument>,

    val tenant_details: TenantRoomDetails?
)