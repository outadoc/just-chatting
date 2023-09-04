plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.compose)
    alias(libs.plugins.moko.resources)
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

                api(project(":feature-home-presentation"))
                api(project(":feature-chat-presentation-mobile"))
                api(project(":feature-preferences-presentation-mobile"))
                implementation(project(":component-chatapi-twitch"))
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.accompanist.placeholder)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.paging.compose)
                implementation(libs.androidx.splashscreen)
                implementation(libs.androidx.emoji2.core)
                implementation(libs.compose.ui.core)
                implementation(libs.coil.compose)
                implementation(libs.koin.compose)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material.core2)
                implementation(libs.compose.material.core3)
                implementation(libs.compose.material.icons)
                implementation(libs.compose.material.windowSizeClass)
                implementation(libs.compose.ui.core)
                implementation(libs.compose.ui.tooling)
                implementation(libs.kotlinx.datetime)
                implementation(libs.moko.resources.core)
                implementation(libs.moko.resources.compose)
            }
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "fr.outadoc.justchatting.feature.home.presentation.mobile"
}

android {
    namespace = "fr.outadoc.justchatting.feature.home.presentation.mobile"
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
    implementation(platform(libs.compose.bom))
    coreLibraryDesugaring(libs.desugar)
}
