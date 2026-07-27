package com.xvantage.rental.ui.auth.fragment.sealed

/**
 * Project: Rental App By XV Team
 * Author: Mujammil x Vipul x XV Team
 * Date:  03/02/25
 * <p>
 * Licensed under the Apache License, Version 2.0. See LICENSE file for terms.
 */


sealed class AuthScreen {

    object SignIn : AuthScreen()

    data class VerifyOtp(
        val phone: String,
        val isFromLogin: Boolean
    ) : AuthScreen()

    // Shown ONLY for brand-new users, right after OTP verify, BEFORE CreateProfile.
    object RoleSelection : AuthScreen()

    object CreateProfile : AuthScreen()
    object Dashboard : AuthScreen()
}