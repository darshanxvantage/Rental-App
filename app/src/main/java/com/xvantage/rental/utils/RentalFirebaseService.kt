package com.xvantage.rental.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class RentalFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Rental App"
        val body = remoteMessage.notification?.body ?: ""
        RentalNotificationHelper.showRentDue(
            context = this,
            tenantName = title,
            amount = body
        )
    }

    override fun onNewToken(token: String) {
        AppPreference(this).setFcmToken(token)
    }
}