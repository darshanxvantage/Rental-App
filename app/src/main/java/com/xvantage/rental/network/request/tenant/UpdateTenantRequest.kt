package com.xvantage.rental.network.request.tenant

import okhttp3.MultipartBody

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

    val rentSubmissionDate: String,

    val fixedWaterBillAmount: String,

    val fixedElectricityAmount: String,

    val meterReading: String,

    val waterReading: String,

    val costPerUnit: String,

    val costUnitWater: String,

    val leaseType: String,

    val leaseEndDate: String,

    val profilePic: MultipartBody.Part?,

    val documents: List<MultipartBody.Part>?
)