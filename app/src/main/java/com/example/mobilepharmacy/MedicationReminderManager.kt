package com.example.mobilepharmacy

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

class MedicationReminderManager(private val context: Context) {

    fun scheduleMedicationReminder(doseTime: String) {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis
        val doseTimeParts = doseTime.split(":")
        if (doseTimeParts.size == 2) {
            val doseHour = doseTimeParts[0].toInt()
            val doseMinute = doseTimeParts[1].toInt()

            calendar.set(Calendar.HOUR_OF_DAY, doseHour)
            calendar.set(Calendar.MINUTE, doseMinute)
            calendar.set(Calendar.SECOND, 0)


            if (calendar.timeInMillis > currentTime) {
                val reminderIntent = Intent(context, MedicationReminderReceiver::class.java)
                reminderIntent.putExtra(MedicationReminderReceiver.EXTRA_DOSE_TIME, doseTime)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    reminderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                alarmManager?.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}
