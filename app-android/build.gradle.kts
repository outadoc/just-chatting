plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.screenshot)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "fr.outadoc.justchatting"
    compileSdk = 37

    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    defaultConfig {
        applicationId = "fr.outadoc.justchatting"
        minSdk = 23
        targetSdk = 37
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
        compose = true
    }
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":shared-ui"))
    implementation(libs.compose.runtime)

    screenshotTestImplementation(project(":shared-ui"))
    screenshotTestImplementation(libs.compose.ui)
    screenshotTestImplementation(libs.compose.ui.tooling)
    screenshotTestImplementation(libs.compose.ui.tooling.preview)
    screenshotTestImplementation(libs.compose.material3)
    screenshotTestImplementation(libs.screenshot.validation.api)
}
