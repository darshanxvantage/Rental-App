package com.xvantage.rental.network.response

data class PropertyDetailsResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: PropertyDetailsData?
)

data class PropertyDetailsData(

    val id: String,

    val name: String,

    val address: String,

    val propertyType: String,

    val ownerName: String,

    val waNumber: String,

    val propertyImage: String,

    val totalRooms: Int,

    val totalTenants: Int,

    val occupiedRooms: Int,

    val vacantRooms: Int,

    val rooms: List<PropertyRoomItem>
)

data class PropertyRoomItem(

    val id: String,

    val room_no: String,

    val status: String
)