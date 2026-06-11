package com.xvantage.rental.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.xvantage.rental.ui.dashboard.DashboardActivity

object RentalNotificationHelper {

    private const val CHANNEL_RENT = "rent_due_channel"
    private const val CHANNEL_PUSH = "push_channel"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_RENT,
                    "Rent Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_PUSH,
                    "Push Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }

    fun showRentDue(context: Context, tenantName: String, amount: String) {
        val pref = AppPreference(context)
        if (!pref.isPushNotifEnabled()) return
        if (isDND(pref)) return

        val intent = Intent(context, DashboardActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_RENT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Rent Due Reminder")
            .setContentText("$tenantName ka ₹$amount baki hai!")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        val mgr = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        mgr.notify(tenantName.hashCode(), notif)
    }

    fun showPaymentReceived(context: Context, tenantName: String, amount: String) {
        val pref = AppPreference(context)
        if (!pref.isPushNotifEnabled()) return

        val notif = NotificationCompat.Builder(context, CHANNEL_PUSH)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Payment Received!")
            .setContentText("$tenantName ne ₹$amount diya")
            .setAutoCancel(true)
            .build()

        val mgr = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        mgr.notify(System.currentTimeMillis().toInt(), notif)
    }

    private fun isDND(pref: AppPreference): Boolean {
        if (!pref.isDNDEnabled()) return false
        val hour = java.util.Calendar.getInstance()
            .get(java.util.Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 8
    }
}