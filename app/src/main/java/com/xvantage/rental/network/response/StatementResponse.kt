package com.xvantage.rental.network.response

data class StatementResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: StatementData
)

data class StatementData(
    val filePath: String
)