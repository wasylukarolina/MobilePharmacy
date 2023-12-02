package com.example.mobilepharmacy

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagePush : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle data payload of FCM messages.
        if (remoteMessage.data.isNotEmpty()) {
            // Handle the data message here.
        }

        // Handle notification payload of FCM messages.
        remoteMessage.notification?.let {
            // Handle the notification message here.
        }
    }
}