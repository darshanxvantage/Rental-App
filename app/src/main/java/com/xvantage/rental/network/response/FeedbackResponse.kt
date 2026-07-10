package com.xvantage.rental.network.response

data class FeedbackResponse(
    val success: Boolean,
    val message: String,
    val data: FeedbackData?
)

data class FeedbackData(
    val id: String?,
    val category: String?,
    val rating: Int?,
    val message: String?,
    val status: String?
)