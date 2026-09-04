package kr.co.gpja.timetable.widget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId

data class Lesson(val date: String, val grade: String, val className: String, val period: Int, val subject: String, val teacher: String = "")

object TimetableRepository {
    private const val URL_STRING = "https://test20000-hub.github.io/GPJA_timetable/data/timetable.json"
    suspend fun today(): List<Lesson> = withContext(Dispatchers.IO) {
        val conn = URL(URL_STRING).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000; conn.readTimeout = 8000
        try {
            val json = conn.inputStream.bufferedReader().use { it.readText() }
            val rows = JSONObject(json).optJSONArray("rows") ?: return@withContext emptyList()
            val date = LocalDate.now(ZoneId.of("Asia/Seoul")).toString(); val result = mutableListOf<Lesson>()
            for (i in 0 until rows.length()) {
                val r = rows.getJSONObject(i)
                if (r.optString("date") == date && r.optString("grade") == "1" && r.optString("className") == "5") {
                    result += Lesson(date, "1", "5", r.optInt("period"), r.optString("subject", "미정"), r.optString("teacher", ""))
                }
            }
            result.sortedBy { it.period }
        } finally { conn.disconnect() }
    }
}
