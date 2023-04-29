@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "fr.outadoc.justchatting.feature.chat.presentation"
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

dependencies {
    api(project(":feature-chat-domain"))

    implementation(project(":feature-pronouns-domain"))
    implementation(project(":feature-pronouns-presentation"))

    implementation(project(":component-chatapi-common"))
    implementation(project(":component-chatapi-domain"))
    implementation(project(":component-chatapi-twitch"))
    implementation(project(":component-preferences-domain"))

    implementation(project(":utils-core"))
    implementation(project(":utils-ui"))
    implementation(project(":utils-logging"))

    implementation(platform(libs.compose.bom))

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.coil.core)
    implementation(libs.compose.runtime.core)
    implementation(libs.compose.ui.core)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(libs.koin.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.io)
    implementation(libs.okio)

    testImplementation(libs.junit)

    coreLibraryDesugaring(libs.desugar)
}
