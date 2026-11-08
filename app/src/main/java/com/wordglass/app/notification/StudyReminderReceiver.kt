package com.wordglass.app.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


/**
 * Daily study reminder via Android AlarmManager + Notification.
 *
 * The reminder fires once a day at the user-chosen hour. The receiver posts a notification
 * asking the user to come back and study. Permission to schedule exact alarms is requested
 * by the host activity when the user enables the reminder.
 */
class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "该学习啦"
        val text = intent.getStringExtra(EXTRA_TEXT) ?: "每天进步一点点，坚持就是胜利。"

        ensureChannel(context)
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pending = PendingIntent.getActivity(
            context,
            0,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "学习提醒",
                        NotificationManager.IMPORTANCE_DEFAULT,
                    ).apply { description = "每天定时提醒你学习单词" },
                )
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "wordglass_reminder"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val REQUEST_CODE = 2001

        /** Schedule a daily reminder at [hour]:[minute]. */
        fun schedule(context: Context, hour: Int, minute: Int, enabled: Boolean) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pending = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, StudyReminderReceiver::class.java)
                    .putExtra(EXTRA_TITLE, "该学习啦")
                    .putExtra(EXTRA_TEXT, "每天进步一点点，坚持就是胜利。"),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            if (!enabled) {
                alarm.cancel(pending)
                return
            }
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                if (before(java.util.Calendar.getInstance())) add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            alarm.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pending,
            )

            // Also schedule smart review check every 4 hours
            val smartPending = PendingIntent.getBroadcast(
                context,
                SMART_REQUEST_CODE,
                Intent(context, SmartReviewReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            alarm.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 30 * 60 * 1000, // first check in 30 min
                4 * 60 * 60 * 1000, // every 4 hours
                smartPending,
            )
        }

        /** Cancel all reminders. */
        fun cancelAll(context: Context) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.cancel(PendingIntent.getBroadcast(
                context, REQUEST_CODE,
                Intent(context, StudyReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ))
            alarm.cancel(PendingIntent.getBroadcast(
                context, SMART_REQUEST_CODE,
                Intent(context, SmartReviewReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ))
        }

        const val SMART_REQUEST_CODE = 2002
    }
}

