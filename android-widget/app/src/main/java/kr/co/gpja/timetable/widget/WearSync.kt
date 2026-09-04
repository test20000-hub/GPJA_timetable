package kr.co.gpja.timetable.widget

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId

object WearSync {
    const val PATH_SYNC = "/gpja/sync"
    const val PATH_REQUEST_SYNC = "/gpja/request/sync"
    const val PATH_ACK = "/gpja/ack"
    const val PROTOCOL_VERSION = 1

    suspend fun sendSync(context: Context) = withContext(Dispatchers.IO) {
        val date = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val lessons = TimetableRepository.today()
        val meals = fetchMeals(date.toString().replace("-", ""))
        val request = PutDataMapRequest.create(PATH_SYNC)
        request.dataMap.putInt("protocolVersion", PROTOCOL_VERSION)
        request.dataMap.putString("date", date.toString())
        request.dataMap.putString("scheduleJson", lessonsToJson(lessons))
        request.dataMap.putString("mealJson", meals)
        request.dataMap.putLong("sentAt", System.currentTimeMillis())
        request.setUrgent()
        Wearable.getDataClient(context).putDataItem(request.asPutDataRequest()).await()
    }

    private fun lessonsToJson(lessons: List<Lesson>): String {
        val array = JSONArray()
        lessons.forEach { lesson ->
            array.put(JSONObject().apply {
                put("period", lesson.period)
                put("subject", lesson.subject)
                put("teacher", lesson.teacher)
            })
        }
        return array.toString()
    }

    private fun fetchMeals(date: String): String {
        val url = java.net.URL("https://kschoolinfo.com/api/v1/meals?eduCode=J10&schoolCode=7531272&date=$date")
        val conn = url.openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")
        return try {
            if (conn.responseCode !in 200..299) "{\"ok\":false,\"data\":[]}" else conn.inputStream.bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            "{\"ok\":false,\"data\":[]}"
        } finally {
            conn.disconnect()
        }
    }
}

class PhoneWearListenerService : com.google.android.gms.wearable.WearableListenerService() {
    override fun onMessageReceived(event: com.google.android.gms.wearable.MessageEvent) {
        if (event.path != WearSync.PATH_REQUEST_SYNC) return
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            runCatching { WearSync.sendSync(this@PhoneWearListenerService) }
        }
    }

    override fun onDataChanged(events: com.google.android.gms.wearable.DataEventBuffer) {
        events.forEach { event ->
            if (event.type == com.google.android.gms.wearable.DataEvent.TYPE_CHANGED && event.dataItem.uri.path == WearSync.PATH_ACK) {
                getSharedPreferences("wear_sync", Context.MODE_PRIVATE).edit()
                    .putLong("lastAckAt", System.currentTimeMillis()).apply()
            }
        }
    }
}
