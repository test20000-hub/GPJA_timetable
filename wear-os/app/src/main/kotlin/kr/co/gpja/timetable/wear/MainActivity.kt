package kr.co.gpja.timetable.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.launch
import org.json.JSONArray

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WatchHome() }
    }
}

@Composable
private fun WatchHome() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("wear_sync", android.content.Context.MODE_PRIVATE) }
    var receivedAt by remember { mutableLongStateOf(prefs.getLong("receivedAt", 0L)) }
    var schedule by remember { mutableStateOf(readSchedule(prefs.getString("scheduleJson", "[]"))) }
    var meal by remember { mutableStateOf(readMeal(prefs.getString("mealJson", ""))) }
    var status by remember { mutableStateOf(if (receivedAt > 0) "휴대폰과 동기화됨" else "휴대폰과 연결하세요") }
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            Text("GPJA TIMETABLE")
            Text(status)
            if (schedule.isNotEmpty()) {
                schedule.take(4).forEach { Text("${it.first}교시  ${it.second}") }
            } else {
                Text("시간표 데이터 없음")
            }
            Text(if (meal.isBlank()) "급식 데이터 없음" else "급식  $meal")
            Button(onClick = {
                status = "동기화 요청 중..."
                scope.launch {
                    runCatching { WearSync.requestSync(context) }
                        .onSuccess { status = "동기화 요청 완료" }
                        .onFailure { status = "휴대폰 연결을 확인하세요" }
                    receivedAt = prefs.getLong("receivedAt", receivedAt)
                    schedule = readSchedule(prefs.getString("scheduleJson", "[]"))
                    meal = readMeal(prefs.getString("mealJson", ""))
                }
            }) { Text("휴대폰과 동기화") }
        }
    }
}

private fun readSchedule(raw: String?): List<Pair<Int, String>> = runCatching {
    val a = JSONArray(raw ?: "[]")
    buildList {
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            add(o.optInt("period") to o.optString("subject", "미정"))
        }
    }.sortedBy { it.first }
}.getOrDefault(emptyList())

private fun readMeal(raw: String?): String = runCatching {
    val root = org.json.JSONObject(raw ?: "")
    val rows = root.optJSONArray("data") ?: return@runCatching ""
    val names = mutableListOf<String>()
    for (i in 0 until rows.length()) {
        val menu = rows.getJSONObject(i).optJSONArray("menu") ?: continue
        for (j in 0 until menu.length()) names += menu.getJSONObject(j).optString("name")
    }
    names.joinToString(" · ")
}.getOrDefault("")
