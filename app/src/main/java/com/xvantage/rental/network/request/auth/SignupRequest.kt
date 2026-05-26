
package com.xvantage.rental.network.request.auth

import com.google.gson.annotations.SerializedName

data class SignupRequest(

    @SerializedName("phoneNumber")
    val phoneNumber: String,


    @SerializedName("deviceType")
    val deviceType: String,

    @SerializedName("deviceName")
    val deviceName: String,

    @SerializedName("androidVersion")
    val androidVersion: String
)
