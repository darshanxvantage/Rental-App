package com.xvantage.rental.network.request.tenant

import android.net.Uri

data class UpdateTenantRequest(

    val tenantId: String,

    val tenantName: String,

    val phoneNumber: String,

    val propertyId: String,

    val roomId: String,

    val rent: String,

    val deposit: String,

    val profilePic: Uri?
)