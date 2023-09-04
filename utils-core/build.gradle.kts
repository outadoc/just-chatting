plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    ios()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {}
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.moko.resources.core)
            }
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting.utils.core"
    compileSdkVersion = "android-34"

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
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
