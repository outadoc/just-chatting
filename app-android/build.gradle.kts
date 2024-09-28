plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spotless)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.moko.resources)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
        }

        androidMain.dependencies {
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.foundation)

            implementation(libs.accompanist.systemuicontroller)
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
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.paging.compose.common)
            implementation(libs.androidx.palette)
            implementation(libs.androidx.splashscreen)
            implementation(libs.coil.core)
            implementation(libs.coil.gif)
            implementation(libs.compose.material.windowSizeClass)
            implementation(libs.compose.runtime.livedata)
            implementation(libs.compose.ui.core)
            implementation(libs.compose.ui.tooling)
            implementation(libs.firebase.crashlytics)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.material.core)
            implementation(libs.moko.resources.core)
            implementation(libs.moko.resources.compose)
            implementation(libs.okio)
            implementation(libs.uri.kmp)
        }
    }
}

multiplatformResources {
    resourcesPackage.set("fr.outadoc.justchatting")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources.excludes += "DebugProbesKt.bin"
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(platform(libs.kotlin.bom))
    coreLibraryDesugaring(libs.desugar)
}
