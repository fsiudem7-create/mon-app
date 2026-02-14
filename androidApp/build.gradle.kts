plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.ui)
    implementation(compose.material3)
    implementation(compose.uiToolingPreview)
    implementation("androidx.activity:activity-compose:1.9.2")
}

android {
    namespace = "com.planboss.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.planboss"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures { compose = true }
}
