package kr.co.gpja.timetable.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class TimetableAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_NOTIFY) return
        val period = intent.getIntExtra("period", 1)
        val next = if (period < 7) period + 1 else null
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("timetable", "시간표 알림", NotificationManager.IMPORTANCE_DEFAULT))
        val open = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tab", "timetable")
        }
        val pending = PendingIntent.getActivity(context, 5000 + period, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val text = if (next != null) "쉬는 시간 · 다음 " + next + "교시 시간표를 확인하세요" else "오늘 수업이 끝났습니다 · 내일 시간표를 확인하세요"
        nm.notify(5000 + period, NotificationCompat.Builder(context, "timetable")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("군포중앙고 시간표")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text + "\n알림을 누르면 시간표 화면으로 이동합니다."))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build())
    }
    companion object {
        const val ACTION_REFRESH = "kr.co.gpja.timetable.widget.ACTION_REFRESH"
        const val ACTION_NOTIFY = "kr.co.gpja.timetable.widget.ACTION_NOTIFY"
    }
}
