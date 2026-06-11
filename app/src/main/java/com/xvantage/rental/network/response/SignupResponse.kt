package com.xvantage.rental.network.response

data class  SignupResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>,
    val err: Map<String, Any>
)