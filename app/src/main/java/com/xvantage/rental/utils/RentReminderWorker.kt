package com.xvantage.rental.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class RentReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val tenantName = inputData.getString("tenant_name") ?: "Tenant"
        val amount = inputData.getString("amount") ?: "0"
        RentalNotificationHelper.showRentDue(context, tenantName, amount)
        return Result.success()
    }
}