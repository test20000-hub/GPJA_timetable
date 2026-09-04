package kr.co.gpja.timetable.widget

import android.content.BroadcastReceiver
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class TimetableAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TimetableWidgetReceiver::class.java))
            if (ids.isNotEmpty()) TimetableWidget.update(context, manager, ids)
        }
    }

    companion object {
        const val ACTION_REFRESH = "kr.co.gpja.timetable.widget.ACTION_REFRESH"
    }
}
