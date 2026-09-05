package kr.co.gpja.timetable.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class TimetableWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        // Keep launcher callback isolated: rendering must never be allowed to crash
        // the receiver, and scheduling must not be part of the critical add-widget path.
        try {
            TimetableWidget.update(context, manager, ids)
        } catch (_: Throwable) {
            ids.forEach { id ->
                try { TimetableWidget.update(context, manager, intArrayOf(id)) } catch (_: Throwable) { }
            }
        }
        try { TimetableScheduler.schedule(context.applicationContext) } catch (_: Throwable) { }
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            super.onReceive(context, intent)
        } catch (_: Throwable) {
            return
        }
        if (intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_TIME_TICK
        ) {
            try {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, TimetableWidgetReceiver::class.java))
                if (ids.isNotEmpty()) TimetableWidget.update(context, manager, ids)
            } catch (_: Throwable) { }
        }
    }
}
