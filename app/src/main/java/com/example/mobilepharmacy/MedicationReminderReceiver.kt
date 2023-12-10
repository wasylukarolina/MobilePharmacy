//package com.example.mobilepharmacy
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//
//class MedicationReminderReceiver : BroadcastReceiver() {
//
//    override fun onReceive(context: Context, intent: Intent) {
//        val doseTime = intent.getStringExtra(EXTRA_DOSE_TIME)
//        Log.d(TAG, "Przypomnienie o przyjęciu leku: $doseTime")
//
//        // Wywołaj metodę do wysyłania powiadomienia
//        showNotification(context, doseTime)
//    }
//
//    private fun showNotification(context: Context, doseTime: String?) {
//        val channelId = "medication_reminder_channel"
//        val channelName = "com.example.mobilepharmacy.Medication Reminder"
//
//        val notificationBuilder = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.notification_icon)
//            .setContentTitle("Przypomnienie o przyjęciu leku")
//            .setContentText("Czas na przyjęcie leku o godzinie $doseTime")
//            .setAutoCancel(true)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//
//        val notificationManager = NotificationManagerCompat.from(context)
//        createNotificationChannel(context, channelId, channelName)
//        notificationManager.notify(0, notificationBuilder.build())
//    }
//
//    private fun createNotificationChannel(context: Context, channelId: String, channelName: String) {
//        // Tworzenie kanału powiadomień (jeśli używasz wersji Androida >= Oreo)
//    }
//
//    companion object {
//        private const val TAG = "MedicationReminder"
//        const val EXTRA_DOSE_TIME = "extra.dose_time"
//    }
//}
