package com.xvantage.rental.network.response

data class TenantListResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: TenantListData
)

data class TenantListData(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val rows: List<TenantItem>
)

data class TenantItem(

    val id: String,

    val tenant_name: String,

    val phone_number: String?,

    val status: String?,

    val profile_pic: String?,

    // Rent Details
    val rent: String?,
    val room_deposit: String?,
    val amount: String?,
    val advance: String?,
    val payment_due: String?,

    // Date Details
    val checkin_date: String?,
    val rent_start_date: String?,
    val rent_receive_date: String?,
    val rent_end_date: String?,

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

    // Other
    val payment_mode: String?,
    val note: String?,

    val documents: List<TenantDocument>,

    val property_fk: String,

    val room_fk: String,

    val tenant_details: TenantRoomDetails?,

    // Populated only by GET /landlord/tenant/dues — null/absent when
    // this TenantItem comes from the regular tenant list endpoint.
    val dueCycles: List<DueCycle>? = null,
    val totalDue: Double? = null,
    val hasOverdue: Boolean? = null,
    val dueMonthsCount: Int? = null
)

data class DueCycle(
    val id: String,
    val cycleMonth: String,
    val monthLabel: String,
    val dueDate: String,
    val totalAmount: Double,
    val amountPaid: Double,
    val amountDue: Double,
    val status: String,
    val isOverdue: Boolean,
    val isProrated: Boolean
)

data class TenantDuesResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: TenantDuesData
)

data class TenantDuesData(
    val tenants: List<TenantItem>
)

data class TenantDocument(

    val id: String?,

    val image: String?
)

data class TenantRoomDetails(

    val room_no: String?,

    val property: TenantProperty?
)

data class TenantProperty(

    val name: String?
)