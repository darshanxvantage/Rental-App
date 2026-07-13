package com.xvantage.rental.network.response

data class PropertyListResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: PropertyListData
)

data class PropertyListData(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val rows: List<PropertyItem>
)

data class PropertyItem(
    val id: String,
    val name: String,
    val address: String,
    val no_of_room: String,
    val total_tenants: Int,
    val wa_number: String? = "",
    val property_room_no: List<PropertyRoom>,
    val property_images: List<PropertyImage> = emptyList()
)

data class PropertyImage(
    val image: String
)

data class PropertyRoom(
    val id: String,
    val room_no: String,
    val status: String,
    val rent: String? = null
)