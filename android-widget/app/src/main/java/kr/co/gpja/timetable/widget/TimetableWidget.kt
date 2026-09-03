package kr.co.gpja.timetable.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import java.time.LocalTime

class TimetableWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        update(context, manager, ids)
    }

    override fun onEnabled(context: Context) { TimetableScheduler.schedule(context) }

    companion object {
        private const val PREFS = "widget_prefs"

        fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val now = LocalTime.now()
            val current = currentPeriod(now)
            val next = nextPeriod(now)
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

            ids.forEach { id ->
                val style = prefs.getString("style_$id", "light") ?: "light"
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                applyStyle(views, style)
                views.setTextViewText(R.id.widget_school, "군포중앙고 · 1학년 5반")
                views.setTextViewText(R.id.widget_badge, if (style == "glass") "GLASS" else "시간표")

                when {
                    current != null -> {
                        views.setTextViewText(R.id.widget_title, "${current}교시 진행 중")
                        views.setTextViewText(R.id.widget_time, "${format(startTime(current))} – ${format(endTime(current))}")
                        views.setTextViewText(R.id.widget_subject, WidgetData.summary(context).removePrefix("${current}교시  "))
                        views.setTextViewText(R.id.widget_hint, "탭해서 전체 시간표 보기")
                    }
                    next != null -> {
                        views.setTextViewText(R.id.widget_title, "다음 ${next}교시")
                        views.setTextViewText(R.id.widget_time, "${format(startTime(next))} – ${format(endTime(next))}")
                        views.setTextViewText(R.id.widget_subject, WidgetData.summary(context).removePrefix("다음 ${next}교시  "))
                        views.setTextViewText(R.id.widget_hint, "수업 전에 확인하세요")
                    }
                    else -> {
                        views.setTextViewText(R.id.widget_title, "오늘 수업 종료")
                        views.setTextViewText(R.id.widget_time, "")
                        views.setTextViewText(R.id.widget_subject, "오늘 수업이 모두 끝났습니다.")
                        views.setTextViewText(R.id.widget_hint, "탭해서 내일 시간표 확인")
                    }
                }

                val openIntent = Intent(context, MainActivity::class.java)
                val pending = PendingIntent.getActivity(context, id, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(R.id.widget_root, pending)
                manager.updateAppWidget(id, views)
            }
        }

        private fun applyStyle(v: RemoteViews, style: String) {
            val s = when (style) {
                "dark" -> Style(R.drawable.widget_bg_dark, Color.WHITE, Color.rgb(190, 194, 205), Color.rgb(147, 197, 253), 22f, 16)
                "color" -> Style(R.drawable.widget_bg_color, Color.rgb(15, 55, 95), Color.rgb(55, 95, 130), Color.rgb(37, 99, 235), 22f, 16)
                "minimal" -> Style(R.drawable.widget_bg_minimal, Color.rgb(30, 30, 30), Color.rgb(100, 100, 100), Color.rgb(71, 85, 105), 20f, 13)
                "glass" -> Style(R.drawable.widget_bg_glass, Color.WHITE, Color.rgb(235, 242, 255), Color.WHITE, 22f, 16)
                "transparent" -> Style(android.R.color.transparent, Color.WHITE, Color.rgb(210, 210, 218), Color.WHITE, 21f, 12)
                else -> Style(R.drawable.widget_bg_light, Color.rgb(15, 23, 42), Color.rgb(100, 116, 139), Color.rgb(37, 99, 235), 22f, 16)
            }
            v.setInt(R.id.widget_root, "setBackgroundResource", s.background)
            v.setTextColor(R.id.widget_school, s.secondary)
            v.setTextColor(R.id.widget_title, s.primary)
            v.setTextColor(R.id.widget_time, s.secondary)
            v.setTextColor(R.id.widget_subject, s.primary)
            v.setTextColor(R.id.widget_badge, s.accent)
            v.setTextColor(R.id.widget_hint, s.secondary)
            v.setTextViewTextSize(R.id.widget_title, android.util.TypedValue.COMPLEX_UNIT_SP, s.titleSize)
            v.setViewPadding(R.id.widget_root, s.padding, s.padding, s.padding, s.padding)
        }

        private data class Style(val background: Int, val primary: Int, val secondary: Int, val accent: Int, val titleSize: Float, val padding: Int)
        private fun currentPeriod(t: LocalTime): Int? = (1..7).firstOrNull { !t.isBefore(startTime(it)) && t.isBefore(endTime(it)) }
        private fun nextPeriod(t: LocalTime): Int? = (1..7).firstOrNull { t.isBefore(startTime(it)) }
        private fun startTime(p: Int) = when (p) { 1 -> LocalTime.of(9,10); 2 -> LocalTime.of(10,10); 3 -> LocalTime.of(11,10); 4 -> LocalTime.of(13,0); 5 -> LocalTime.of(14,0); 6 -> LocalTime.of(15,0); else -> LocalTime.of(16,0) }
        private fun endTime(p: Int) = when (p) { 1 -> LocalTime.of(10,0); 2 -> LocalTime.of(11,0); 3 -> LocalTime.of(12,0); 4 -> LocalTime.of(13,50); 5 -> LocalTime.of(14,50); 6 -> LocalTime.of(15,50); else -> LocalTime.of(16,50) }
        private fun format(t: LocalTime) = "%02d:%02d".format(t.hour, t.minute)
    }
}
