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

    val documents: List<TenantDocument>,

    val property_fk: String,

    val tenant_details: TenantRoomDetails?
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