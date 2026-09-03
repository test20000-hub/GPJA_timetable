package kr.co.gpja.timetable.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class TimetableWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TimetableWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            TimetableScheduler.schedule(context)
        }
    }
}
