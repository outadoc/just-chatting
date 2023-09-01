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
                implementation(project(":utils-ui"))
                implementation(project(":utils-core"))
                implementation(project(":utils-logging"))

                implementation(project(":component-deeplink"))
                implementation(project(":component-chatapi-common"))
                implementation(project(":component-chatapi-twitch"))
                implementation(project(":component-preferences-domain"))
                implementation(project(":component-preferences-data"))

                implementation(project(":feature-chat-domain"))
                api(project(":feature-chat-presentation"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.accompanist.placeholder)
                implementation(libs.accompanist.systemuicontroller)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.emoji2.core)
                implementation(libs.androidx.palette)
                implementation(libs.androidx.splashscreen)
                implementation(libs.coil.compose)
                implementation(libs.compose.material.core2)
                implementation(libs.compose.material.core3)
                implementation(libs.compose.material.icons)
                implementation(libs.compose.ui.core)
                implementation(libs.compose.ui.tooling)
                implementation(libs.koin.compose)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.datetime)
                implementation(libs.material.core)
            }
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting.feature.chat.presentation.mobile"
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
