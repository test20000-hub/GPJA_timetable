package kr.co.gpja.timetable.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZonedDateTime

object TimetableScheduler {
    // 알림은 각 교시 종료 시점에 울려 다음 교시를 안내합니다.
    private val ends = intArrayOf(
        10 * 60,
        11 * 60,
        12 * 60,
        13 * 60 + 50,
        14 * 60 + 50,
        15 * 60 + 50
    )

    fun schedule(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val now = ZonedDateTime.now()
        ends.forEachIndexed { i, minute ->
            var at = now.withHour(minute / 60).withMinute(minute % 60).withSecond(0).withNano(0)
            if (!at.isAfter(now)) at = at.plusDays(1)
            val intent = Intent(context, TimetableAlarmReceiver::class.java)
                .putExtra("period", i + 1)
            val pi = PendingIntent.getBroadcast(
                context,
                i + 1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarm.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                at.toInstant().toEpochMilli(),
                pi
            )
        }
    }
}
