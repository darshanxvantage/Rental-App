package com.xvantage.rental.network.response

data class DashboardResponse(
    val success: Boolean,
    val message: String,
    val data: DashboardData
)

data class DashboardData(

    val month: String,

    val cycleMonth: String,

    val totalProperties: Int,

    val totalTenants: Int,

    val monthlyRentTotal: Double,

    val monthCollected: Double,

    val monthPending: Double,

    val collectionRatePercent: Int,

    val lifetimeRevenue: Double,

    val lifetimeRentTotal: Double,

    val totalRentCollected: Double,

    val totalRentDue: Double

)