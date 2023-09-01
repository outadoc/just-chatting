plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.sqldelight)
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
        val commonMain by getting {
            dependencies {
                implementation(project(":utils-core"))
                implementation(project(":utils-logging"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.compose.runtime.core)
                implementation(libs.irc)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.datetime)
            }
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting.component.chatapi.db"
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

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("fr.outadoc.justchatting.component.chatapi.db")
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    coreLibraryDesugaring(libs.desugar)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.koin.core)
}
