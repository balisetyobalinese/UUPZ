plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "store.tv"
    compileSdk = 35
    defaultConfig {
        applicationId = "store.tv"
        minSdk = 28 // Android 9 Pie
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures { viewBinding = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.leanback:leanback:1.2.0")
    implementation("androidx.leanback:leanback-preference:1.2.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.code.gson:gson:2.10.1")
}
