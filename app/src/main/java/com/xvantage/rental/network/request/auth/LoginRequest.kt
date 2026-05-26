package com.xvantage.rental.network.request.auth

data class LoginRequest(

    val phoneNumber: String,
    val deviceName: String,
    val androidVersion: String,
    val otp: String = ""

)

