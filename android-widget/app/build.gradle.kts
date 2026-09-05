plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "kr.co.gpja.timetable.widget"
    compileSdk = 35
    buildFeatures { buildConfig = true }
    defaultConfig {
        applicationId = "kr.co.gpja.timetable.widget"
        minSdk = 26
        targetSdk = 35
        versionCode = 29
        versionName = "2.0.9"

        val adminCode = providers.environmentVariable("ADMIN_CODE").orNull ?: ""
        val escapedAdminCode = adminCode.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
        buildConfigField("String", "ADMIN_CODE", "\"$escapedAdminCode\"")
    }

    val keystoreFile = providers.environmentVariable("SIGNING_KEYSTORE_FILE").orNull
    val storePassword = providers.environmentVariable("SIGNING_STORE_PASSWORD").orNull
    val keyAlias = providers.environmentVariable("SIGNING_KEY_ALIAS").orNull
    val keyPassword = providers.environmentVariable("SIGNING_KEY_PASSWORD").orNull

    signingConfigs {
        if (!keystoreFile.isNullOrBlank() && !storePassword.isNullOrBlank() && !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()) {
            create("release") {
                storeFile = file(keystoreFile)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            val releaseSigning = signingConfigs.findByName("release")
                ?: throw GradleException("Persistent release signing is not configured. Set SIGNING_KEYSTORE_FILE, SIGNING_STORE_PASSWORD, SIGNING_KEY_ALIAS and SIGNING_KEY_PASSWORD.")
            signingConfig = releaseSigning
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")
}
