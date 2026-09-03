plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android { namespace = "kr.co.gpja.timetable.widget"; compileSdk = 35
    defaultConfig { applicationId = "kr.co.gpja.timetable.widget"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0" }
}

dependencies { implementation("androidx.core:core-ktx:1.15.0"); implementation("androidx.glance:glance-appwidget:1.1.1"); implementation("androidx.glance:glance:1.1.1"); implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1") }
