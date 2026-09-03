package kr.co.gpja.timetable.widget

import android.content.Context
import kotlinx.coroutines.runBlocking
import java.time.LocalTime

object WidgetData {
    fun summary(context: Context): String {
        val lessons = runBlocking { TimetableRepository.today() }
        val now = LocalTime.now()
        val current = lessons.firstOrNull { it.period == currentPeriod(now) }
        val next = lessons.firstOrNull { it.period > (currentPeriod(now) ?: 0) }
        return when {
            current != null -> "${current.period}교시  ${current.subject}${if (current.teacher.isNotBlank()) " · ${current.teacher}" else ""}"
            next != null -> "다음 ${next.period}교시  ${next.subject}"
            lessons.isEmpty() -> "오늘 시간표가 없습니다"
            now.isBefore(LocalTime.of(9,10)) -> "1교시  ${lessons.firstOrNull()?.subject ?: "미정"}"
            else -> "오늘 수업이 끝났습니다"
        }
    }
    fun currentPeriod(t: LocalTime): Int? = (1..7).firstOrNull { !t.isBefore(start(it)) && t.isBefore(end(it)) }
    private fun start(p:Int)=when(p){1->LocalTime.of(9,10);2->LocalTime.of(10,10);3->LocalTime.of(11,10);4->LocalTime.of(13);5->LocalTime.of(14);6->LocalTime.of(15);else->LocalTime.of(16)}
    private fun end(p:Int)=when(p){1->LocalTime.of(10);2->LocalTime.of(11);3->LocalTime.of(12);4->LocalTime.of(13,50);5->LocalTime.of(14,50);6->LocalTime.of(15,50);else->LocalTime.of(16,50)}
}
