package com.xvantage.rental.network.request.feedback

data class FeedbackRequest(
    val category: String,      // BUG | SUGGESTION | COMPLAINT | COMPLIMENT | GENERAL
    val rating: Int?,          // 1 to 5, optional
    val message: String,
    val appVersion: String? = null
)