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
                implementation(project(":component-chatapi-common"))
                implementation(project(":component-chatapi-twitch"))
                implementation(project(":component-chatapi-db"))

                implementation(project(":component-preferences-domain"))

                implementation(project(":utils-core"))
                implementation(project(":utils-logging"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.room.core)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.paging.runtime)
                implementation(libs.compose.runtime.core)
                implementation(libs.irc)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.datetime)
            }
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting.component.chatapi.domain"
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
