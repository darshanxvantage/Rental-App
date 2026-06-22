package com.xvantage.rental.network.request.tenant

data class TenantPaymentRequest(

    val tenantId: String,

    val roomId: String,

    val amount: String,

    val rent_receive_date: String,

    val rent_start_date: String,

    val rent_end_date: String,

    val payment_mode: String,

    val note: String

)