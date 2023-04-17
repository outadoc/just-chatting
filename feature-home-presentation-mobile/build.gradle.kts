@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "fr.outadoc.justchatting.feature.home.presentation.mobile"
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
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
}

dependencies {
    implementation(project(":utils-ui"))
    implementation(project(":utils-core"))

    api(project(":feature-home-presentation"))
    api(project(":feature-chat-presentation-mobile"))
    api(project(":feature-preferences-presentation-mobile"))
    implementation(project(":component-chatapi-twitch"))

    implementation(platform(libs.compose.bom))

    implementation(libs.accompanist.placeholder)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.emoji2.core)
    implementation(libs.compose.ui.core)
    implementation(libs.coil.compose)
    implementation(libs.koin.compose)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.core2)
    implementation(libs.compose.material.core3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material.windowSizeClass)
    implementation(libs.compose.ui.core)
    implementation(libs.compose.ui.tooling)
    implementation(libs.kotlinx.datetime)

    coreLibraryDesugaring(libs.desugar)
}
