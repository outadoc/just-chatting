plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.moko.resources)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.skie)
}

kotlin {
    explicitApi()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }

        compilerOptions {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=fr.outadoc.justchatting.utils.parcel.Parcelize",
            )
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries {
            framework {
                baseName = "JCShared"
                isStatic = false

                export(libs.kotlinx.coroutines)
                export(libs.kotlinx.datetime)
                export(libs.kotlinx.serialization.json)
                export(libs.ktor.client.core)
                export(libs.moko.resources.core)
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines)
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.serialization.json)
            api(libs.ktor.client.core)
            api(libs.moko.resources.core)

            implementation(compose.runtime)

            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.paging.compose.common)
            implementation(libs.bignum)
            implementation(libs.fluid.currency)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.contentNegociation)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.serialization)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.stately.common)
            implementation(libs.uri.kmp)
            implementation(libs.unicode.codepoints)
        }

        androidMain.dependencies {
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.uiTooling)
            implementation(libs.compose.material.windowSizeClass)
            implementation(libs.compose.navigation)

            implementation(libs.accompanist.permissions)
            implementation(libs.accompanist.placeholder)
            implementation(libs.accompanist.systemuicontroller)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.emoji2.core)
            implementation(libs.androidx.palette)
            implementation(libs.androidx.paging.runtime.android)
            implementation(libs.androidx.splashscreen)
            implementation(libs.coil.compose)
            implementation(libs.koin.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.material.core)
            implementation(libs.moko.resources.compose)
            implementation(libs.okhttp)

            api(libs.sqldelight.driver.android)
        }

        iosMain.dependencies {
            api(libs.sqldelight.driver.native)
            implementation(libs.androidx.paging.runtime.ios)
            implementation(libs.ktor.client.darwin)
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
    }
}

multiplatformResources {
    resourcesPackage.set("fr.outadoc.justchatting.shared")
}

android {
    namespace = "fr.outadoc.justchatting.shared"
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
            packageName.set("fr.outadoc.justchatting.data.db")
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}
