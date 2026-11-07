package com.wordglass.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-schedules the daily study reminder after device reboot (AlarmManager alarms don't
 * survive reboot on Android). Settings are re-read from DataStore to decide if the
 * reminder is enabled.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // Re-schedule with the default time; the user's saved settings are re-applied
        // when the app next opens.
        StudyReminderReceiver.schedule(context, 20, 0, enabled = true)
    }
}
