package com.xvantage.rental.network.request.tenant

data class CreateTenantRequest(

    val room_fk: String,

    val property_fk: String,

    val tenant_name: String,

    val phone_number: String,

    val phone_code: String? = null,

    val rent: String,

    val room_deposit: String,

    val checkin_date: String,

    val rent_start_date: String,

    val rent_submission_date: String,

    val meter_reading: String = "0",

    val meter_reading_water: String = "0",

    val cost_per_unit: String,

    val cost_unit_water: String,

    val fixed_waterbill: String,

    val fixed_waterbill_amount: String,

    val fixed_electricity: String,

    val fixed_electricity_amount: String,

    val advance: String = "0",

    val refrence_name: String = "",

    val profile_pic: String? = null
)