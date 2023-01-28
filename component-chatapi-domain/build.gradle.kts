@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "fr.outadoc.justchatting.component.chatapi.domain"
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
    implementation(project(":component-chatapi-twitch"))
    implementation(project(":component-chatapi-db"))

    implementation(project(":component-preferences-domain"))

    implementation(project(":utils-core"))
    implementation(project(":utils-logging"))

    implementation(platform(libs.compose.bom))

    implementation(libs.androidx.room.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.compose.runtime.core)
    implementation(libs.irc)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.datetime)

    coreLibraryDesugaring(libs.desugar)
}
