@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.diffplug.spotless")
}

android {
    namespace = "fr.outadoc.justchatting"
    compileSdkVersion = "android-33"

    defaultConfig {
        applicationId = "fr.outadoc.justchatting"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            signingConfig = signingConfigs.getByName("debug")
        }

        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0-alpha02"
    }

    signingConfigs {
        named("debug") {
            keyAlias = "debug"
            keyPassword = ""
            storeFile = rootProject.file("keystores/debug.p12")
            storePassword = "android"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir.absolutePath}/compose_metrics",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.buildDir.absolutePath}/compose_metrics"
        )
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint("0.45.2").userData(mapOf("disabled_rules" to "no-wildcard-imports"))
        endWithNewline()
    }

    json {
        target("**/*.json")
        simple().indentWithSpaces(2)
    }
}

dependencies {
    implementation(platform(libs.kotlin.bom))

    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.webview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.emoji2.core)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.room.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.splashscreen)
    implementation(libs.coil.compose)
    implementation(libs.coil.core)
    implementation(libs.coil.gif)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.core2)
    implementation(libs.compose.material.core3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material.windowSizeClass)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.ui.core)
    implementation(libs.compose.ui.tooling)
    implementation(libs.irc)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(libs.material.core)
    implementation(libs.okhttp.logging)
    implementation(libs.okio)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)

    ksp(libs.androidx.room.compiler)

    // debugImplementation(libs.leakcanary)
    debugImplementation(libs.chucker.runtime)
    releaseImplementation(libs.chucker.noop)

    testImplementation(libs.junit)

    coreLibraryDesugaring(libs.desugar)
}
