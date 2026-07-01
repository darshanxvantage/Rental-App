package com.xvantage.rental.network.response

data class PaymentSuccessResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentData
)

data class PaymentData(
    val paymentId: String?,
    val tenant: TenantItem?,
    val clearedCycleMonths: List<String> = emptyList(),
    val invoiceFilePath: String?,
    val paymentSummary: PaymentSummary?  // ← nullable banaya
)

data class PaymentSummary(
    val amountReceived: Double = 0.0,
    val previousDue: Double = 0.0,
    val remainingDue: Double = 0.0,
    val isFullyPaid: Boolean = false,
    val advance: Double = 0.0           // ← default value 0.0
)