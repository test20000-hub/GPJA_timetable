package kr.co.gpja.timetable.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class TimetableAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val period = intent.getIntExtra("period", 1)
        val next = if (period < 7) period + 1 else 7
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("timetable", "시간표 알림", NotificationManager.IMPORTANCE_DEFAULT))
        nm.notify(period, NotificationCompat.Builder(context, "timetable")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("군포중앙고 시간표")
            .setContentText("쉬는 시간입니다 · 다음 ${next}교시 시간표를 확인하세요")
            .setAutoCancel(true).build())
        TimetableScheduler.schedule(context)
    }
}
