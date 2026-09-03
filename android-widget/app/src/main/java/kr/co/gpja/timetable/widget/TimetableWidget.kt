package kr.co.gpja.timetable.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import java.time.LocalTime

class TimetableWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        WidgetContent()
    }

    @Composable private fun WidgetContent() {
        val now = LocalTime.now()
        val current = currentPeriod(now)
        val next = nextPeriod(now)
        val target = current ?: next
        val title = when {
            current != null -> "${current}교시 진행 중"
            next != null -> "다음 ${next}교시"
            else -> "오늘 수업 종료"
        }
        val times = target?.let { "${start(it)}–${end(it)}" } ?: ""
        val subject = when (target) {
            null -> "오늘 수업이 모두 끝났습니다."
            else -> "시간표를 열어 과목을 확인하세요."
        }
        Column(
            GlanceModifier.fillMaxSize().background(ColorProvider(android.graphics.Color.WHITE)).padding(16.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text("군포중앙고 · 1학년 5반", style = TextStyle(fontSize = 13.sp, color = ColorProvider(android.graphics.Color.DKGRAY)))
            Spacer(GlanceModifier.height(5.dp))
            Text(title, style = TextStyle(fontSize = 19.sp))
            Text(times, style = TextStyle(fontSize = 12.sp, color = ColorProvider(android.graphics.Color.GRAY)))
            Spacer(GlanceModifier.height(4.dp))
            Text(subject, style = TextStyle(fontSize = 12.sp, color = ColorProvider(android.graphics.Color.DKGRAY)))
        }
    }

    private fun currentPeriod(t: LocalTime): Int? = (1..7).firstOrNull { within(it, t) }
    private fun nextPeriod(t: LocalTime): Int? = (1..7).firstOrNull { t.isBefore(startTime(it)) }
    private fun within(p: Int, t: LocalTime) = !t.isBefore(startTime(p)) && t.isBefore(endTime(p))
    private fun startTime(p: Int) = when (p) { 1 -> LocalTime.of(9,10); 2 -> LocalTime.of(10,10); 3 -> LocalTime.of(11,10); 4 -> LocalTime.of(13,0); 5 -> LocalTime.of(14,0); 6 -> LocalTime.of(15,0); else -> LocalTime.of(16,0) }
    private fun endTime(p: Int) = when (p) { 1 -> LocalTime.of(10,0); 2 -> LocalTime.of(11,0); 3 -> LocalTime.of(12,0); 4 -> LocalTime.of(13,50); 5 -> LocalTime.of(14,50); 6 -> LocalTime.of(15,50); else -> LocalTime.of(16,50) }
    private fun start(p: Int) = "%02d:%02d".format(startTime(p).hour, startTime(p).minute)
    private fun end(p: Int) = "%02d:%02d".format(endTime(p).hour, endTime(p).minute)
}
