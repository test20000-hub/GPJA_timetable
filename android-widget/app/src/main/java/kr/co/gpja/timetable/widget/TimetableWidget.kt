package kr.co.gpja.timetable.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Color
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
        private const val PREFS = "widget_prefs"

        fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val now = LocalTime.now()
            val current = currentPeriod(now)
            val next = nextPeriod(now)
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

            ids.forEach { appWidgetId ->
                val style = prefs.getString("style_$appWidgetId", "light") ?: "light"
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                applyStyle(views, style)
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
                manager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun applyStyle(views: RemoteViews, style: String) {
            val (background, primary, secondary, titleSize, padding) = when (style) {
                "dark" -> Style(Color.rgb(25, 25, 28), Color.WHITE, Color.LTGRAY, 20f, 16)
                "color" -> Style(Color.rgb(225, 240, 255), Color.rgb(15, 55, 95), Color.rgb(55, 95, 130), 20f, 16)
                "minimal" -> Style(Color.rgb(245, 245, 245), Color.rgb(30, 30, 30), Color.rgb(100, 100, 100), 18f, 12)
                else -> Style(Color.WHITE, Color.rgb(17, 17, 17), Color.rgb(100, 100, 100), 20f, 16)
            }
            views.setInt(R.id.widget_root, "setBackgroundColor", background)
            views.setTextColor(R.id.widget_school, secondary)
            views.setTextColor(R.id.widget_title, primary)
            views.setTextColor(R.id.widget_time, secondary)
            views.setTextColor(R.id.widget_subject, primary)
            views.setTextViewTextSize(R.id.widget_title, android.util.TypedValue.COMPLEX_UNIT_SP, titleSize)
            views.setViewPadding(R.id.widget_root, padding, padding, padding, padding)
        }

        private data class Style(val background: Int, val primary: Int, val secondary: Int, val titleSize: Float, val padding: Int)

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
