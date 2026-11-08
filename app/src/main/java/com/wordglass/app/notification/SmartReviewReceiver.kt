package com.wordglass.app.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Smart review reminder: checks if there are words due for review
 * and sends a notification if so.
 */
class SmartReviewReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = com.wordglass.app.data.local.WordDatabase.create(context)
                val states = db.wordDao().allStates()
                val now = System.currentTimeMillis()
                val dueCount = states.count { it.nextReviewAt != null && it.nextReviewAt <= now }

                if (dueCount > 0) {
                    showNotification(context, dueCount)
                }
            } catch (_: Exception) { }
        }
    }

    private fun showNotification(context: Context, dueCount: Int) {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending = PendingIntent.getActivity(
            context,
            0,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, StudyReminderReceiver.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("该复习啦！")
            .setContentText("有 $dueCount 个单词等你复习，点开开始吧")
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) { }
    }

    companion object {
        const val NOTIFICATION_ID = 1002
    }
}
