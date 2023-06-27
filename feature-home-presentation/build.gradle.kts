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
                implementation(project(":utils-logging"))

                implementation(project(":component-chatapi-domain"))
                implementation(project(":component-preferences-domain"))
                implementation(project(":component-deeplink"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.paging.runtime)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
            }
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting.feature.home.presentation"
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
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
