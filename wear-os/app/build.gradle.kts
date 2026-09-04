plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "kr.co.gpja.timetable.wear"
    compileSdk = 35

    defaultConfig {
        applicationId = "kr.co.gpja.timetable.wear"
        minSdk = 30
        targetSdk = 35
        versionCode = 9
        versionName = "1.0.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }

    lint {
        // lifecycle 2.8.7's NullSafeMutableLiveData detector crashes with
        // Kotlin 2.1/UAST during release lint analysis. This detector is not
        // applicable to this Wear OS app and is disabled to unblock release builds.
        disable += "NullSafeMutableLiveData"
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.foundation:foundation:1.7.8")
    implementation("androidx.wear.compose:compose-material3:1.6.2")
    implementation("androidx.wear.compose:compose-foundation:1.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
}
