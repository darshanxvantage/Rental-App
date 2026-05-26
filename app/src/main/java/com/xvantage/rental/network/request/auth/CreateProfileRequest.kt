
package com.xvantage.rental.network.request.auth

data class CreateProfileRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val state: String,
    val city: String,
    val age: Int
)

