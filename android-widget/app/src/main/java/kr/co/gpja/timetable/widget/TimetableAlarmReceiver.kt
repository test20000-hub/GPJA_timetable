package kr.co.gpja.timetable.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.runBlocking

class TimetableAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TimetableWidget::class.java))
            if (ids.isNotEmpty()) TimetableWidget.update(context, manager, ids)
            return
        }
        if (intent.action != ACTION_NOTIFY) return
        val period = intent.getIntExtra("period", 1)
        val lessons = runBlocking { TimetableRepository.today() }
        val nextPeriod = (period + 1).takeIf { it <= 7 }
        val next = nextPeriod?.let { p -> lessons.firstOrNull { it.period == p } }
        val remaining = nextPeriod?.let { p -> lessons.filter { it.period > p } } ?: emptyList()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("timetable", "시간표 알림", NotificationManager.IMPORTANCE_DEFAULT))
        val open = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tab", "timetable")
        }
        val pending = PendingIntent.getActivity(context, 5000 + period, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val bigText = if (next != null) buildString {
            append("쉬는 시간 · 다음 " + next.period + "교시\n\n")
            append("📚 " + next.period + "교시 · " + next.subject + "\n")
            if (next.teacher.isNotBlank()) append("👨‍🏫 " + next.teacher + " 선생님\n")
            append("🕐 " + time(next.period))
            if (remaining.isNotEmpty()) {
                append("\n\n오늘 남은 시간표")
                remaining.forEach { lesson ->
                    append("\n" + lesson.period + "교시 · " + lesson.subject)
                    if (lesson.teacher.isNotBlank()) append(" · " + lesson.teacher)
                }
            }
        } else "오늘 수업이 종료되었습니다.\n\n오늘의 모든 수업이 끝났습니다.\n내일 시간표를 확인해 보세요."

        val shortText = if (next != null) "쉬는 시간 · 다음 " + next.period + "교시 " + next.subject
        else "오늘 수업이 종료되었습니다 · 내일 시간표를 확인하세요"

        nm.notify(5000 + period, NotificationCompat.Builder(context, "timetable")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("군포중앙고 시간표")
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build())
    }

    private fun time(period: Int): String = when (period) {
        1 -> "09:10 ~ 10:00"; 2 -> "10:10 ~ 11:00"; 3 -> "11:10 ~ 12:00"
        4 -> "13:00 ~ 13:50"; 5 -> "14:00 ~ 14:50"; 6 -> "15:00 ~ 15:50"
        else -> "16:00 ~ 16:50"
    }

    companion object {
        const val ACTION_REFRESH = "kr.co.gpja.timetable.widget.ACTION_REFRESH"
        const val ACTION_NOTIFY = "kr.co.gpja.timetable.widget.ACTION_NOTIFY"
    }
}