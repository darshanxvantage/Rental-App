package com.xvantage.rental.network.response

data class InvoiceHistoryResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: InvoiceHistoryData
)

data class InvoiceHistoryData(
    val invoices: List<InvoiceHistoryEntry>
)

data class InvoiceHistoryEntry(
    val id: String,
    val invoiceNumber: String,
    val monthLabel: String?,
    val amount: Double,
    val filePath: String,
    val createdAt: String,
    val tenantId: String?,
    val tenantName: String?,
    val roomNo: String?,
    val propertyName: String?,
    val propertyAddress: String?
)