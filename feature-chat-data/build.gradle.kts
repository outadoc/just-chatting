plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
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
                implementation(project(":utils-core"))

                implementation(project(":component-preferences-domain"))
                implementation(project(":component-chatapi-twitch"))
                implementation(project(":component-chatapi-common"))
                implementation(project(":component-chatapi-domain"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.coil.core)
                implementation(libs.compose.runtime.core)
                implementation(libs.irc)
                implementation(libs.koin.android)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
            }
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting.feature.chat.data"
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
    implementation(platform(libs.compose.bom))
    coreLibraryDesugaring(libs.desugar)
}
