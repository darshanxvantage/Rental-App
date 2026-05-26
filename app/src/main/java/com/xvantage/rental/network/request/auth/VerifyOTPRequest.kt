package com.xvantage.rental.network.request.auth

/**
 * Project: Rental App By XV Team
 * Author: Mujammil x Vipul x XV Team
 * Date:  07/02/25
 * <p>
 * Licensed under the Apache License, Version 2.0. See LICENSE file for terms.
 */

data class VerifyOTPRequest(

    val phoneNumber: String,
    val otp: String,
    val deviceType: String = "android"

)


