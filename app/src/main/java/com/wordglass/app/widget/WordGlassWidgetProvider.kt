package com.wordglass.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.wordglass.app.MainActivity
import com.wordglass.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home screen widget showing today's learning progress.
 * Uses RemoteViews for maximum HyperOS/MIUI compatibility.
 */
class WordGlassWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // Show placeholder immediately to avoid "loading" error
        val sdf = SimpleDateFormat("M月d日", Locale.CHINESE)
        for (appWidgetId in appWidgetIds) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_wordglass)
                views.setTextViewText(R.id.widget_title, "WordGlass · ${sdf.format(Date())}")
                views.setTextViewText(R.id.widget_progress, "加载中…")
                views.setTextViewText(R.id.widget_due_count, "-")
                views.setTextViewText(R.id.widget_learned_count, "-")
                views.setTextViewText(R.id.widget_mastered_count, "-")

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("wordglass_start", "study")
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                views.setOnClickPendingIntent(R.id.widget_action, pendingIntent)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) {
                // Silent fail - system will retry
            }
        }

        // Load real data in background
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = com.wordglass.app.data.local.WordDatabase.create(context)
                val words = db.wordDao().allWords()
                val states = db.wordDao().allStates()

                val now = System.currentTimeMillis()
                val dueCount = states.count { it.nextReviewAt != null && it.nextReviewAt <= now }
                val learnedCount = states.count { it.masteryLevel > 0 }
                val masteredCount = states.count { it.masteryLevel >= 3 }
                val dailyGoal = try {
                    val prefs = context.getSharedPreferences("widget_cache", Context.MODE_PRIVATE)
                    prefs.getInt("daily_goal", 25)
                } catch (_: Exception) { 25 }

                for (appWidgetId in appWidgetIds) {
                    try {
                        val views = RemoteViews(context.packageName, R.layout.widget_wordglass)
                        views.setTextViewText(R.id.widget_title, "WordGlass · ${sdf.format(Date())}")
                        views.setTextViewText(R.id.widget_progress, "$learnedCount / $dailyGoal 词")
                        views.setTextViewText(R.id.widget_due_count, "$dueCount")
                        views.setTextViewText(R.id.widget_learned_count, "$learnedCount")
                        views.setTextViewText(R.id.widget_mastered_count, "$masteredCount")

                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("wordglass_start", "study")
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            appWidgetId,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        )
                        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                        views.setOnClickPendingIntent(R.id.widget_action, pendingIntent)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    } catch (_: Exception) { }
                }
            } catch (_: Exception) {
                // Keep placeholder visible on error
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }
}
