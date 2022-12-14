@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "fr.outadoc.justchatting.feature.preferences.presentation.mobile"
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0-alpha02"
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    api(project(":feature-preferences-presentation"))

    implementation(libs.compose.ui.core)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines)

    coreLibraryDesugaring(libs.desugar)
}
