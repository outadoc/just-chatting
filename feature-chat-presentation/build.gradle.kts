plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
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
                api(project(":feature-chat-domain"))

                implementation(project(":feature-pronouns-domain"))

                implementation(project(":component-chatapi-common"))
                implementation(project(":component-chatapi-domain"))
                implementation(project(":component-preferences-domain"))

                implementation(project(":utils-core"))
                implementation(project(":utils-ui"))
                implementation(project(":utils-logging"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.annotation)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.coil.core)
                implementation(libs.compose.runtime.core)
                implementation(libs.compose.ui.core)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.android)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.io)
                implementation(libs.okio)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting.feature.chat.presentation"
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
