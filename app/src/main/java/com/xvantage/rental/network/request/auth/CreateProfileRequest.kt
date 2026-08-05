
package com.xvantage.rental.network.request.auth

data class CreateProfileRequest(

    val firstName: String,

    val lastName: String,

    val gender: String,

    val dob: String,

    val email: String

)
