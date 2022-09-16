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
        kotlinCompilerExtensionVersion = "1.3.1"
    }

    signingConfigs {
        named("debug") {
            keyAlias = "debug"
            keyPassword = "123456"
            storeFile = file("debug-keystore.jks")
            storePassword = "123456"
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
    implementation(project(":lib-irc"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.emoji2.core)
    implementation(libs.androidx.emoji2.views)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.paging)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.room.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.webkit)
    implementation(libs.coil.compose)
    implementation(libs.coil.core)
    implementation(libs.coil.gif)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.core3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.ui.core)
    implementation(libs.compose.ui.tooling)
    implementation(libs.fragnav)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(libs.material.core)
    implementation(libs.material.themeadapter)
    implementation(libs.okhttp.logging)
    implementation(libs.okio)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)

    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.chucker.runtime)
    releaseImplementation(libs.chucker.noop)

    testImplementation(libs.junit)

    coreLibraryDesugaring(libs.desugar)
}
