package kr.co.gpja.timetable.widget

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
import java.time.LocalDate
import java.time.LocalTime

class TimetableWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) = provideContent {
        WidgetContent()
    }

    @Composable private fun WidgetContent() {
        val now = LocalTime.now()
        val period = currentPeriod(now)
        val next = nextPeriod(now)
        val title = if (period != null) "${period}교시 진행 중" else if (next != null) "다음 ${next}교시" else "오늘 수업 종료"
        val times = when (next ?: period) { 1 -> "09:10–10:00"; 2 -> "10:10–11:00"; 3 -> "11:10–12:00"; 4 -> "13:00–13:50"; 5 -> "14:00–14:50"; 6 -> "15:00–15:50"; 7 -> "16:00–16:50"; else -> "" }
        Column(GlanceModifier.fillMaxSize().background(ColorProvider(android.graphics.Color.WHITE)).padding(16.dp), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Text("군포중앙고 · 1학년 5반", style = TextStyle(fontSize = 13.sp, color = ColorProvider(android.graphics.Color.DKGRAY)))
            Spacer(GlanceModifier.height(5.dp)); Text(title, style = TextStyle(fontSize = 19.sp)); Text(times, style = TextStyle(fontSize = 12.sp, color = ColorProvider(android.graphics.Color.GRAY)))
        }
    }

    private fun currentPeriod(t: LocalTime): Int? = listOf(9 to 10,10 to 11,11 to 12,13 to 13,14 to 14,15 to 15,16 to 16).mapIndexed { i, _ -> i + 1 }.firstOrNull { p -> within(p,t) }
    private fun nextPeriod(t: LocalTime): Int? = (1..7).firstOrNull { p -> start(p).isAfter(t) }
    private fun within(p:Int,t:LocalTime):Boolean = !t.isBefore(start(p)) && t.isBefore(end(p))
    private fun start(p:Int)=when(p){1->LocalTime.of(9,10);2->LocalTime.of(10,10);3->LocalTime.of(11,10);4->LocalTime.of(13);5->LocalTime.of(14);6->LocalTime.of(15);else->LocalTime.of(16)}
    private fun end(p:Int)=when(p){1->LocalTime.of(10);2->LocalTime.of(11);3->LocalTime.of(12);4->LocalTime.of(13,50);5->LocalTime.of(14,50);6->LocalTime.of(15,50);else->LocalTime.of(16,50)}
}
