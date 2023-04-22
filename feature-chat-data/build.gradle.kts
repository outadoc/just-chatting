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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":utils-logging"))
    implementation(project(":utils-core"))
    implementation(project(":component-preferences-domain"))
    implementation(project(":component-chatapi-twitch"))
    implementation(project(":component-chatapi-common"))
    implementation(project(":component-chatapi-domain"))

    implementation(platform(libs.compose.bom))

    implementation(libs.androidx.core)
    implementation(libs.coil.core)
    implementation(libs.compose.runtime.core)
    implementation(libs.irc)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)

    coreLibraryDesugaring(libs.desugar)
}
