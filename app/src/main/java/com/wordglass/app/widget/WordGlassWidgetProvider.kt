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

/**
 * Compact home screen widget (2x1 cells).
 * Shows: progress, due count, learned, mastered.
 */
class WordGlassWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // Immediate placeholder
        for (id in appWidgetIds) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_wordglass)
                views.setTextViewText(R.id.widget_title, "WordGlass")
                views.setTextViewText(R.id.widget_progress, "加载中…")
                views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context, id))
                appWidgetManager.updateAppWidget(id, views)
            } catch (_: Exception) {}
        }

        // Load real data
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = com.wordglass.app.data.local.WordDatabase.create(context)
                val states = db.wordDao().allStates()
                val now = System.currentTimeMillis()
                val due = states.count { it.nextReviewAt != null && it.nextReviewAt <= now }
                val learned = states.count { it.masteryLevel > 0 }
                val mastered = states.count { it.masteryLevel >= 3 }

                for (id in appWidgetIds) {
                    try {
                        val views = RemoteViews(context.packageName, R.layout.widget_wordglass)
                        views.setTextViewText(R.id.widget_title, "WordGlass")
                        views.setTextViewText(R.id.widget_progress, "$learned / 25 词")
                        views.setTextViewText(R.id.widget_due_count, "$due 待复习")
                        views.setTextViewText(R.id.widget_learned_count, "$learned 已学")
                        views.setTextViewText(R.id.widget_mastered_count, "$mastered 已掌握")
                        views.setOnClickPendingIntent(R.id.widget_root, getPendingIntent(context, id))
                        appWidgetManager.updateAppWidget(id, views)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    private fun getPendingIntent(context: Context, id: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("wordglass_start", "study")
        }
        return PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
