package com.xvantage.rental.network.request.property

import android.net.Uri

data class UpdatePropertyRequest(

    val propertyId: String,

    val address: String,

    val noOfRoom: Int,

    val propertyTypeId: String,

    val wa_number: String,

    val name: String,

    val imageUri: Uri?,

    // City / State / Pincode — structured address alongside the free-text address line
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,

    // Bhada House / Row House: rent the whole property as one unit, or unit-wise
    val rentMode: String? = null,

    // Row House: floor structure, e.g. "G+2"
    val floorConfig: String? = null,

    // Commercial (Shops): maintenance charge applied across all shops
    val maintenanceCharge: String? = null,
    val unitNumbers: List<String> = emptyList(),
    val sharingType: String? = null,
    val bedCount: Int? = null
)
