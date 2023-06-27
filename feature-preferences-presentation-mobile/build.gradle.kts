plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
}

kotlin {
    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":feature-preferences-presentation"))
                implementation(project(":utils-ui"))
                implementation(project(":component-preferences-data"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.accompanist.permissions)
                implementation(libs.compose.ui.core)
                implementation(libs.koin.compose)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material.core3)
                implementation(libs.compose.material.icons)
                implementation(libs.compose.ui.core)
                implementation(libs.compose.ui.tooling)
                implementation(libs.kotlinx.datetime)
            }
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting.feature.preferences.presentation.mobile"
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    coreLibraryDesugaring(libs.desugar)
}
