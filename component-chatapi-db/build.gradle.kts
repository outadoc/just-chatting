@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.outadoc.justchatting.component.chatapi.db"
    compileSdkVersion = "android-33"

    defaultConfig {
        minSdk = 21
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

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(project(":utils-core"))
    implementation(project(":utils-logging"))

    implementation(platform(libs.compose.bom))

    api(libs.androidx.room.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.compose.runtime.core)
    implementation(libs.irc)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.datetime)

    ksp(libs.androidx.room.compiler)

    coreLibraryDesugaring(libs.desugar)
}
