package com.xvantage.rental.network.request.tenant

import android.net.Uri

data class UpdateTenantRequest(

    val propertyId: String,

    val roomId: String,

    val deposit: String,

    val tenantId: String,

    val tenantName: String,

    val phoneNumber: String,

    val phoneCode: String,

    val rent: String,

    val roomDeposit: String,

    val rentStartDate: String,

    val fixedWaterBillAmount: String,

    val fixedElectricityAmount: String,

    val meterReading: String,

    val waterReading: String,

    val costPerUnit: String,

    val costUnitWater: String,

    val profilePic: Uri?,

    val documents: List<Uri>?
)