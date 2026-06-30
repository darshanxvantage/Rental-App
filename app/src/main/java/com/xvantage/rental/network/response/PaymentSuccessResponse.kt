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

    val paymentSummary: PaymentSummary

)

data class PaymentSummary(

    val amountReceived: Double,

    val previousDue: Double,

    val remainingDue: Double,

    val isFullyPaid: Boolean,

    val advance: Double

)