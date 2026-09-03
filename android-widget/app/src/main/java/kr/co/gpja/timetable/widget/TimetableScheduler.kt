package kr.co.gpja.timetable.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.*

object TimetableScheduler {
    private val starts = intArrayOf(9*60+10,10*60+10,11*60+10,13*60,14*60,15*60,16*60)
    fun schedule(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val now = ZonedDateTime.now()
        starts.forEachIndexed { i, minute ->
            var at = now.withHour(minute/60).withMinute(minute%60).withSecond(0).withNano(0)
            if (!at.isAfter(now)) at = at.plusDays(1)
            val intent = Intent(context, TimetableAlarmReceiver::class.java).putExtra("period", i + 1)
            val pi = PendingIntent.getBroadcast(context, i + 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toInstant().toEpochMilli(), pi)
        }
    }
}
