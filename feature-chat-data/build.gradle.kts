@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "fr.outadoc.justchatting.feature.chat.data"
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
    implementation(project(":utils-logging"))
    implementation(project(":utils-core"))
    implementation(project(":component-preferences-domain"))
    implementation(project(":component-chatapi-domain"))

    implementation(libs.compose.runtime.core)
    implementation(libs.irc)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)

    testImplementation(libs.junit)

    coreLibraryDesugaring(libs.desugar)
}
