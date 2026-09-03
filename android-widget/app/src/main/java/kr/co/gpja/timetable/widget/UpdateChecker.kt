package kr.co.gpja.timetable.widget

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

object UpdateChecker {
    private const val RELEASES_URL = "https://api.github.com/repos/test20000-hub/GPJA_timetable/releases/latest"

    fun check(activity: Activity, currentVersion: String) {
        thread {
            try {
                val conn = (URL(RELEASES_URL).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 6000
                    readTimeout = 6000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "GPJA-Timetable")
                }
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                val json = JSONObject(body)
                val tag = json.optString("tag_name", "").removePrefix("v")
                val apkUrl = findApkUrl(json)
                if (tag.isBlank() || apkUrl.isBlank() || compareVersions(tag, currentVersion) <= 0) return@thread
                Handler(Looper.getMainLooper()).post {
                    AlertDialog.Builder(activity)
                        .setTitle("새 버전이 있습니다")
                        .setMessage("현재 버전 $currentVersion\n최신 버전 $tag\n\n최신 APK를 설치하면 앱이 업데이트됩니다.")
                        .setNegativeButton("나중에", null)
                        .setPositiveButton("업데이트") { _, _ ->
                            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)))
                        }
                        .show()
                }
            } catch (_: Exception) { }
        }
    }

    private fun findApkUrl(json: JSONObject): String {
        val assets = json.optJSONArray("assets") ?: return ""
        for (i in 0 until assets.length()) {
            val asset = assets.optJSONObject(i) ?: continue
            val name = asset.optString("name", "").lowercase(Locale.US)
            if (name.endsWith(".apk")) return asset.optString("browser_download_url", "")
        }
        return ""
    }

    private fun compareVersions(a: String, b: String): Int {
        val av = a.split(".").map { it.toIntOrNull() ?: 0 }
        val bv = b.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(av.size, bv.size)) {
            val x = av.getOrElse(i) { 0 }
            val y = bv.getOrElse(i) { 0 }
            if (x != y) return x.compareTo(y)
        }
        return 0
    }
}
