import com.github.jk1.license.render.JsonReportRenderer

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spotless)
    alias(libs.plugins.licenseReport)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.moko.resources)
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
    multiplatformResourcesPackage = "fr.outadoc.justchatting"
}

android {
    namespace = "fr.outadoc.justchatting"
    compileSdkVersion = "android-34"

    defaultConfig {
        applicationId = "fr.outadoc.justchatting"
        minSdk = 21
        targetSdk = 33
        versionCode = (findProperty("externalVersionCode") as String?)?.toInt() ?: 99
        versionName = (findProperty("externalVersionName") as String?) ?: "SNAPSHOT"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            type = "boolean",
            name = "ENABLE_LOGGING",
            value = "true",
        )

        buildConfigField(
            type = "boolean",
            name = "ENABLE_MOCK_IRC_ENDPOINT",
            value = findProperty("enableMockIrcEndpoint") as String,
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
}

licenseReport {
    outputDir = file("src/main/assets").path
    configurations = arrayOf("releaseRuntimeClasspath")
    renderers = arrayOf(JsonReportRenderer("dependencies.json"))
}

tasks.named("generateLicenseReport") {
    outputs.upToDateWhen { false }
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(platform(libs.compose.bom))
    coreLibraryDesugaring(libs.desugar)
}
