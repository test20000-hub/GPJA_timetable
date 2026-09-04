package kr.co.gpja.timetable.widget

import android.content.Context
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object WidgetData {
    private val KST = ZoneId.of("Asia/Seoul")

    fun summary(context: Context): String {
        val lessons = runBlocking { TimetableRepository.today() }
        val now = ZonedDateTime.now(KST).toLocalTime()
        val current = currentPeriod(now)?.let { p -> lessons.firstOrNull { it.period == p } }
        val next = nextPeriod(now)?.let { p -> lessons.firstOrNull { it.period == p } }
        return when {
            current != null -> current.period.toString() + "교시  " + current.subject +
                if (current.teacher.isNotBlank()) " · " + current.teacher else ""
            next != null -> "다음 " + next.period + "교시  " + next.subject
            lessons.isEmpty() -> "오늘 시간표가 없습니다"
            now.isBefore(start(1)) -> "1교시  " + (lessons.firstOrNull()?.subject ?: "미정")
            else -> "오늘 수업이 끝났습니다"
        }
    }

    fun currentPeriod(t: LocalTime): Int? =
        (1..7).firstOrNull { !t.isBefore(start(it)) && t.isBefore(end(it)) }

    fun nextPeriod(t: LocalTime): Int? =
        (1..7).firstOrNull { t.isBefore(start(it)) }

    private fun start(p: Int) = when (p) {
        1 -> LocalTime.of(9, 10)
        2 -> LocalTime.of(10, 10)
        3 -> LocalTime.of(11, 10)
        4 -> LocalTime.of(13, 0)
        5 -> LocalTime.of(14, 0)
        6 -> LocalTime.of(15, 0)
        else -> LocalTime.of(16, 0)
    }

    private fun end(p: Int) = when (p) {
        1 -> LocalTime.of(10, 0)
        2 -> LocalTime.of(11, 0)
        3 -> LocalTime.of(12, 0)
        4 -> LocalTime.of(13, 50)
        5 -> LocalTime.of(14, 50)
        6 -> LocalTime.of(15, 50)
        else -> LocalTime.of(16, 50)
    }
}
