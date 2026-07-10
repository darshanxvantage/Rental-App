package com.xvantage.rental.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches the shape returned by GET /tenant/billing-cycles/{tenantId}
 * (tenant.service.js -> getBillingCyclesForTenant).
 */
data class BillingCycle(
    @SerializedName("id") val id: String,
    @SerializedName("cycleMonth") val cycleMonth: String,       // "2026-05-01"
    @SerializedName("dueDate") val dueDate: String,              // "2026-05-31"
    @SerializedName("rentAmount") val rentAmount: Double,
    @SerializedName("electricityAmount") val electricityAmount: Double,
    @SerializedName("waterAmount") val waterAmount: Double,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("amountPaid") val amountPaid: Double,
    @SerializedName("balanceDue") val balanceDue: Double,
    @SerializedName("status") val status: String,                // "pending" | "partial" | "paid"
    @SerializedName("isProrated") val isProrated: Boolean,
    @SerializedName("proratedDays") val proratedDays: Int?
)

/** Generic API envelope, matching src/response/index.js's success()/badRequest() shape. */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T?
)

data class InvoiceFileResponse(
    @SerializedName("filePath") val filePath: String
)