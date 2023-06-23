package com.example.mobilepharmacy

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Przetwarzaj otrzymane powiadomienie
        Log.d(TAG, "Otrzymano powiadomienie: ${remoteMessage.notification?.title}")
    }

    override fun onNewToken(token: String) {
        // Tutaj możesz obsługiwać otrzymanie nowego tokenu
        Log.d(TAG, "Otrzymano nowy token: $token")
    }

    companion object {
        private const val TAG = "MyFirebaseMessaging"
    }
}
