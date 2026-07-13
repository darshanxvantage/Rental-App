package com.xvantage.rental.network.response

data class VerifyOTPResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: Data?,
    val err: Any?
) {
    data class Data(

        val id: String?,

        val first_name: String?,

        val last_name: String?,

        val phone_number: String?,

        val email: String?,

        val state: String?,

        val city: String?,

        val gender: String?,

        val login_type: String?,

        val is_profile_complete: Boolean,

        val profile_pic: String?,

        val token: String?

    )
}