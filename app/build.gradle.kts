@file:Suppress("UnstableApiUsage")

import com.github.jk1.license.importer.XmlReportImporter
import com.github.jk1.license.render.JsonReportRenderer

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spotless)
    alias(libs.plugins.licenseReport)
}

android {
    namespace = "fr.outadoc.justchatting"
    compileSdkVersion = "android-33"

    defaultConfig {
        applicationId = "fr.outadoc.justchatting"
        minSdk = 21
        targetSdk = 33
        versionCode = 3
        versionName = "0.2.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        named("debug") {
            keyAlias = "debug"
            keyPassword = ""
            storeFile = rootProject.file("keystores/debug.p12")
            storePassword = "android"
        }

        create("release") {
            keyAlias = "upload_key"
            keyPassword = ""
            storeFile = rootProject.file("keystores/release.p12")
            storePassword = findProperty("release_keystore_password") as String?
        }
    }

    buildTypes {
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            signingConfig = signingConfigs.getByName("debug")
        }

        create("qa") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0-alpha02"
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
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${buildDir.resolve("compose/reports")}",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${buildDir.resolve("compose/metrics")}"
        )
    }
}

licenseReport {
    outputDir = file("src/main/assets").path
    configurations = arrayOf("releaseRuntimeClasspath")
    renderers = arrayOf(JsonReportRenderer("dependencies.json"))
}

tasks.named("generateLicenseReport") {
    outputs.upToDateWhen { false }
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

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    ksp(libs.androidx.room.compiler)

    // debugImplementation(libs.leakcanary)
    "debugImplementation"(libs.chucker.runtime)
    "qaImplementation"(libs.chucker.noop)
    "releaseImplementation"(libs.chucker.noop)

    testImplementation(libs.junit)

    coreLibraryDesugaring(libs.desugar)
}
