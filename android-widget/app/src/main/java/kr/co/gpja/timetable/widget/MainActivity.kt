package kr.co.gpja.timetable.widget

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.UUID

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private val prefs by lazy { getSharedPreferences("gpja_device", Context.MODE_PRIVATE) }
    private var pendingPermissionRequest: PermissionRequest? = null

    companion object {
        private const val SITE_URL = "https://test20000-hub.github.io/GPJA_timetable/"
        private const val NOTIFICATION_REQUEST_CODE = 1001
        private const val CAMERA_REQUEST_CODE = 1002
        private const val ADMIN_AUTH_VERSION = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.statusBars())

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            addJavascriptInterface(GpjaBridge(this@MainActivity), "Android")

            ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
                insets
            }

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    if (request?.isForMainFrame == true) showErrorPage()
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    if (request.origin.toString().startsWith("https://test20000-hub.github.io") && request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            runOnUiThread { request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) }
                        } else {
                            pendingPermissionRequest = request
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
                        }
                    } else runOnUiThread { request.deny() }
                }
            }
        }

        setContentView(webView)
        ViewCompat.requestApplyInsets(webView)
        ensureRoleAndLoad()

        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_REQUEST_CODE)
        }
        TimetableScheduler.schedule(this)
    }

    private fun ensureRoleAndLoad() {
        migrateLegacyAdminRole()
        if (!prefs.getBoolean("setup_complete", false)) {
            showRoleDialog()
            return
        }
        loadSite()
    }

    private fun migrateLegacyAdminRole() {
        if (prefs.getString("role", null) == "admin") {
            prefs.edit()
                .remove("role")
                .putBoolean("admin_enabled", true)
                .putInt("admin_auth_version", ADMIN_AUTH_VERSION)
                .putBoolean("setup_complete", true)
                .apply()
        }
        if (prefs.getBoolean("admin_enabled", false) && prefs.getInt("admin_auth_version", 0) != ADMIN_AUTH_VERSION) {
            prefs.edit()
                .putBoolean("admin_enabled", false)
                .putInt("admin_auth_version", 0)
                .apply()
        }
    }

    private fun showRoleDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("앱 사용 설정")
            .setMessage("일반 시간표 앱은 누구나 사용할 수 있습니다. 관리자 기능을 활성화하면 일반 앱을 그대로 사용하면서 QR 승인 관리 기능도 함께 사용할 수 있습니다.")
            .setPositiveButton("관리자 기능도 사용") { _, _ -> showAdminCodeDialog(firstSetup = true) }
            .setNegativeButton("일반 앱으로 사용") { _, _ ->
                prefs.edit().putBoolean("setup_complete", true).putBoolean("admin_enabled", false).apply()
                loadSite()
            }
            .setCancelable(false)
            .show()
    }

    private fun showAdminCodeDialog(firstSetup: Boolean = false) {
        val input = EditText(this).apply {
            hint = "관리자 등록코드"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true)
            setPadding(40, 0, 40, 0)
        }
        val container = LinearLayout(this).apply {
            setPadding(24, 0, 24, 0)
            addView(input, LinearLayout.LayoutParams(-1, 56))
        }
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("관리자 기능 활성화")
            .setMessage("관리자 등록코드를 입력하면 일반 시간표 앱은 그대로 유지되고 관리자 QR 승인 기능이 추가됩니다.")
            .setView(container)
            .setPositiveButton("활성화", null)
            .setNegativeButton(if (firstSetup) "일반 앱으로 사용" else "취소") { _, _ ->
                if (firstSetup) {
                    prefs.edit().putBoolean("setup_complete", true).putBoolean("admin_enabled", false).apply()
                    loadSite()
                }
            }
            .setCancelable(false)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val code = input.text.toString()
                if (BuildConfig.ADMIN_CODE.isNotEmpty() && code == BuildConfig.ADMIN_CODE) {
                    prefs.edit()
                        .putBoolean("setup_complete", true)
                        .putBoolean("admin_enabled", true)
                        .putInt("admin_auth_version", ADMIN_AUTH_VERSION)
                        .apply()
                    dialog.dismiss()
                    Toast.makeText(this, "관리자 기능이 활성화되었습니다.", Toast.LENGTH_SHORT).show()
                    loadSite()
                } else {
                    input.text.clear()
                    input.error = "관리자 등록코드가 올바르지 않습니다."
                }
            }
        }
        dialog.show()
        input.requestFocus()
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    private fun loadSite() {
        webView.loadUrl(SITE_URL)
        UpdateChecker.check(this, BuildConfig.VERSION_NAME)
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            val request = pendingPermissionRequest
            pendingPermissionRequest = null
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED && request != null) request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) else request?.deny()
        }
    }

    private inner class GpjaBridge(private val activity: MainActivity) {
        @JavascriptInterface fun isAdmin(): Boolean = prefs.getBoolean("admin_enabled", false) && prefs.getInt("admin_auth_version", 0) == ADMIN_AUTH_VERSION
        @JavascriptInterface fun isApproved(): Boolean = isAdmin() || prefs.getBoolean("approved", false)
        @JavascriptInterface fun setApproved(value: Boolean) { prefs.edit().putBoolean("approved", value).apply() }
        @JavascriptInterface fun getInstallationId(): String {
            val current = prefs.getString("installation_id", null)
            if (current != null) return current
            val id = UUID.randomUUID().toString()
            prefs.edit().putString("installation_id", id).apply()
            return id
        }
        @JavascriptInterface fun requestCameraPermission() { activity.runOnUiThread { activity.requestCameraPermission() } }
        @JavascriptInterface fun registerAdmin() { activity.runOnUiThread { activity.showAdminCodeDialog(false) } }
    }

    private fun showErrorPage() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 72, 48, 48); gravity = android.view.Gravity.CENTER_HORIZONTAL }
        root.addView(TextView(this).apply { text = "군포중앙고 시간표"; textSize = 26f })
        root.addView(TextView(this).apply { text = "사이트를 불러오지 못했습니다.\n인터넷 연결을 확인한 뒤 다시 시도해 주세요."; textSize = 16f; setPadding(0, 24, 0, 24) })
        root.addView(Button(this).apply { text = "다시 불러오기"; setOnClickListener { loadSite() } })
        setContentView(root)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) { webView.stopLoading(); webView.destroy() }
        super.onDestroy()
    }
}
