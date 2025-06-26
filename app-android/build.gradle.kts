import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.spotless)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
        }

        androidMain.dependencies {
            implementation(libs.firebase.crashlytics)
            implementation(libs.material.core)
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting"
    compileSdkVersion = "android-35"

    defaultConfig {
        applicationId = "fr.outadoc.justchatting"
        minSdk = 21
        targetSdk = 35
        versionCode = (findProperty("externalVersionCode") as String?)?.toInt() ?: 99
        versionName = (findProperty("externalVersionName") as String?) ?: "SNAPSHOT"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            type = "boolean",
            name = "ENABLE_LOGGING",
            value = "true",
        )
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
            storePassword = findProperty("releaseKeystorePassword") as String?
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

            matchingFallbacks += "release"

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            buildConfigField(
                type = "boolean",
                name = "ENABLE_LOGGING",
                value = hasProperty("enableLogging").toString(),
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources.excludes += "DebugProbesKt.bin"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(platform(libs.kotlin.bom))
    coreLibraryDesugaring(libs.desugar)
}
