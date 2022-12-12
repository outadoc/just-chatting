@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "fr.outadoc.justchatting.component.twitch.domain"
    compileSdkVersion = "android-33"

    defaultConfig {
        minSdk = 21
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    api(project(":component-twitch-data"))
    implementation(project(":component-preferences-domain"))

    implementation(project(":utils-core"))
    implementation(project(":utils-logging"))

    implementation(libs.androidx.room.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.compose.runtime.core)
    implementation(libs.gson)
    implementation(libs.irc)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.datetime)

    coreLibraryDesugaring(libs.desugar)
}
