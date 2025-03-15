package com.shalomarputhasingh.daysinyear
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.widget.RemoteViews
import java.util.*
class daysLeft : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleMidnightUpdate(context)
    }
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleMidnightUpdate(context) // Schedule updates when widget is first added
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelMidnightUpdate(context) // Stop updates when the last widget is removed
    }
    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val daysLeft = getDaysLeftInYear()

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.days_left_number, "$daysLeft")

        // Adjust text size dynamically based on widget size
        val textSize = getAdaptiveTextSize(daysLeft)
        views.setTextViewTextSize(R.id.days_left_number, TypedValue.COMPLEX_UNIT_SP, textSize)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getDaysLeftInYear(): Int {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
        return totalDays - dayOfYear
    }

    private fun getAdaptiveTextSize(daysLeft: Int): Float {
        return when {
            daysLeft > 300 -> 50f
            daysLeft > 200 -> 45f
            daysLeft > 100 -> 40f
            else -> 35f
        }
    }
    private fun scheduleMidnightUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, daysLeft::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)  // Set time to midnight
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1) // Move to the next day if it's already past midnight
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelMidnightUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, daysLeft::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}