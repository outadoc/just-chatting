@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "fr.outadoc.justchatting.feature.chat.presentation.mobile"
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
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${buildDir.resolve("compose/reports")}",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${buildDir.resolve("compose/metrics")}"
        )
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(project(":utils-ui"))
    implementation(project(":utils-core"))
    implementation(project(":utils-logging"))

    implementation(project(":component-deeplink"))
    implementation(project(":component-chatapi-common"))
    implementation(project(":component-chatapi-twitch"))
    implementation(project(":component-preferences-domain"))
    implementation(project(":component-preferences-data"))

    implementation(project(":feature-chat-domain"))
    api(project(":feature-chat-presentation"))

    implementation(platform(libs.compose.bom))

    implementation(libs.accompanist.placeholder)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.emoji2.core)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.splashscreen)
    implementation(libs.coil.compose)
    implementation(libs.compose.material.core2)
    implementation(libs.compose.material.core3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.ui.core)
    implementation(libs.compose.ui.tooling)
    implementation(libs.koin.compose)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(libs.material.core)

    coreLibraryDesugaring(libs.desugar)
}
