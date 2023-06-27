plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
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
                implementation(project(":utils-core"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.palette)
                implementation(libs.coil.core)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material.core3)
                implementation(libs.compose.ui.core)
                implementation(libs.compose.ui.tooling)
                implementation(libs.kotlinx.datetime)
            }
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting.utils.ui"
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
