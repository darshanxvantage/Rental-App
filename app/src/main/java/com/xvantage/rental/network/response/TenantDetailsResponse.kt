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

    val status: String?,

    // Rent
    val rent: String?,
    val room_deposit: String?,
    val advance: String?,
    val payment_due: String?,
    val amount: String?,

    // Electricity
    val fixed_electricity_amount: String?,
    val fixed_electricity: String?,
    val last_meter_reading: String?,
    val meter_reading: String?,
    val cost_per_unit: String?,

    // Water
    val fixed_waterbill_amount: String?,
    val fixed_waterbill: String?,
    val last_meter_reading_water: String?,
    val meter_reading_water: String?,
    val cost_unit_water: String?,

    // Dates
    val checkin_date: String?,
    val rent_start_date: String?,
    val rent_receive_date: String?,
    val rent_end_date: String?,

    // Payment
    val payment_mode: String?,

    // Note
    val note: String?,

    val documents: List<TenantDocument>,

    val tenant_details: TenantRoomDetails?
)