package kr.co.gpja.timetable.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.time.LocalTime

class TimetableWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context, manager, ids)
    }

    override fun onEnabled(context: Context) {
        TimetableScheduler.schedule(context)
    }

    companion object {
        fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val now = LocalTime.now()
            val current = currentPeriod(now)
            val next = nextPeriod(now)

            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            views.setTextViewText(R.id.widget_school, "군포중앙고 · 1학년 5반")

            when {
                current != null -> {
                    views.setTextViewText(R.id.widget_title, "${current}교시 진행 중")
                    views.setTextViewText(R.id.widget_time, "${format(startTime(current))}–${format(endTime(current))}")
                    views.setTextViewText(R.id.widget_subject, "시간표를 열어 과목을 확인하세요.")
                }
                next != null -> {
                    views.setTextViewText(R.id.widget_title, "다음 ${next}교시")
                    views.setTextViewText(R.id.widget_time, "${format(startTime(next))}–${format(endTime(next))}")
                    views.setTextViewText(R.id.widget_subject, "시간표를 열어 과목을 확인하세요.")
                }
                else -> {
                    views.setTextViewText(R.id.widget_title, "오늘 수업 종료")
                    views.setTextViewText(R.id.widget_time, "")
                    views.setTextViewText(R.id.widget_subject, "오늘 수업이 모두 끝났습니다.")
                }
            }

            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private fun currentPeriod(t: LocalTime): Int? = (1..7).firstOrNull { within(it, t) }
        private fun nextPeriod(t: LocalTime): Int? = (1..7).firstOrNull { t.isBefore(startTime(it)) }
        private fun within(p: Int, t: LocalTime) = !t.isBefore(startTime(p)) && t.isBefore(endTime(p))
        private fun startTime(p: Int) = when (p) {
            1 -> LocalTime.of(9, 10)
            2 -> LocalTime.of(10, 10)
            3 -> LocalTime.of(11, 10)
            4 -> LocalTime.of(13, 0)
            5 -> LocalTime.of(14, 0)
            6 -> LocalTime.of(15, 0)
            else -> LocalTime.of(16, 0)
        }
        private fun endTime(p: Int) = when (p) {
            1 -> LocalTime.of(10, 0)
            2 -> LocalTime.of(11, 0)
            3 -> LocalTime.of(12, 0)
            4 -> LocalTime.of(13, 50)
            5 -> LocalTime.of(14, 50)
            6 -> LocalTime.of(15, 50)
            else -> LocalTime.of(16, 50)
        }
        private fun format(t: LocalTime) = "%02d:%02d".format(t.hour, t.minute)
    }
}
