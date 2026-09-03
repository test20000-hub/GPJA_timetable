package kr.co.gpja.timetable.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

class WidgetConfigActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setResult(RESULT_CANCELED)

        val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val current = prefs.getString("style_$appWidgetId", "light") ?: "light"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 32)
        }
        root.addView(TextView(this).apply {
            text = "위젯 스타일"
            textSize = 24f
        })
        root.addView(TextView(this).apply {
            text = "홈 화면 위젯의 모양을 선택하세요."
            textSize = 15f
            setPadding(0, 12, 0, 20)
        })

        val styles = arrayOf("밝은색", "다크", "컬러", "미니멀")
        val values = arrayOf("light", "dark", "color", "minimal")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, styles)
        spinner.setSelection(values.indexOf(current).coerceAtLeast(0))
        root.addView(spinner)

        root.addView(Button(this).apply {
            text = "저장"
            setOnClickListener {
                val style = values[spinner.selectedItemPosition]
                prefs.edit().putString("style_$appWidgetId", style).apply()
                TimetableWidget.update(this@WidgetConfigActivity, AppWidgetManager.getInstance(this@WidgetConfigActivity), intArrayOf(appWidgetId))
                val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(RESULT_OK, result)
                finish()
            }
        })

        setContentView(root)
    }
}
