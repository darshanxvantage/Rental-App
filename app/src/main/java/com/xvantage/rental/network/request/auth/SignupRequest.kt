
package com.xvantage.rental.network.request.auth

import com.google.gson.annotations.SerializedName

data class SignupRequest(

    @SerializedName("phoneNumber")
    val phoneNumber: String,


    @SerializedName("device_type")
    val deviceType: String,

    @SerializedName("deviceName")
    val deviceName: String,

    @SerializedName("androidVersion")
    val androidVersion: String
)
