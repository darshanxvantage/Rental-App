
package com.xvantage.rental.network.response

data class CreateProfileResponse(
    val success: Boolean,
    val message: String,
    val data: Data? = null
) {
    data class Data(
        val id: String? = null,
        val first_name: String? = null,
        val last_name: String? = null,
        val phone_number: String? = null,
        val email: String? = null,
        val profile_pic: String? = null,
        val gender: String? = null,
        val dob: String? = null,
        val is_profile_complete: Boolean? = null
    )
}

