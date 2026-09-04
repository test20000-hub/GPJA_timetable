package kr.co.gpja.timetable.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZoneId
import java.time.ZonedDateTime

object TimetableScheduler {
    private const val REFRESH_REQUEST_CODE = 9001
    private const val REFRESH_INTERVAL_MS = 60_000L

    fun schedule(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, TimetableAlarmReceiver::class.java)
            .setAction(TimetableAlarmReceiver.ACTION_REFRESH)
        val pi = PendingIntent.getBroadcast(
            context, REFRESH_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(pi)
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        alarm.setRepeating(
            AlarmManager.RTC_WAKEUP,
            now.plusSeconds(1).toInstant().toEpochMilli(),
            REFRESH_INTERVAL_MS,
            pi
        )
    }
}
