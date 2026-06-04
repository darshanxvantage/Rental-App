package com.xvantage.rental.network.response

data class PropertyListResponse(
    val success: Boolean,
    val data: List<PropertyItem>
)

data class PropertyItem(
    val id: String,
    val name: String,
    val address: String,
    val property_image: String?,
    val total_rooms: Int,
    val total_tenants: Int
)