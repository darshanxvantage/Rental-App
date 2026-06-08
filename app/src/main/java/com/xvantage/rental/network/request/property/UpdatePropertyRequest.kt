package com.xvantage.rental.network.request.property

import android.net.Uri

data class UpdatePropertyRequest(

    val propertyId: String,

    val address: String,

    val noOfRoom: Int,

    val propertyTypeId: String,

    val wa_number: String,

    val name: String,

    val imageUri: Uri?
)