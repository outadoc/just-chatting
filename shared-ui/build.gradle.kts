import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    explicitApi()

    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
        klib {
            keepUnsupportedTargets = false
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=fr.outadoc.justchatting.utils.parcel.Parcelize",
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir}/reports/composeReports",
            )
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries {
            framework {
                baseName = "JCSharedUI"
                isStatic = true
                export(project(":shared"))
            }
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }

        commonMain {
            dependencies {
                api(project(":shared"))

                implementation(libs.androidx.paging.compose.common)
                implementation(libs.coil.compose)
                implementation(libs.coil.core)
                implementation(libs.coil.ktor)
                implementation(libs.haze.core)
                implementation(libs.haze.materials)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.material.adaptive.core)
                implementation(libs.compose.material.adaptive.layout)
                implementation(libs.compose.material.adaptive.navigation)
                implementation(libs.compose.material.adaptive.navigation.suite)
                implementation(libs.compose.material.windowSizeClass)
                implementation(libs.compose.material3)
                implementation(libs.compose.materialIcons)
                implementation(libs.compose.navigation3.ui)
                implementation(libs.compose.runtime)
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.backhandler)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kmpalette.core)
                implementation(libs.kmpalette.extensions.network)
                implementation(libs.material.kolor)
                implementation(libs.uri.kmp)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.accompanist.permissions)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.emoji2.core)
                implementation(libs.androidx.glance.appwidget)
                implementation(libs.androidx.glance.material3)
                implementation(libs.androidx.lifecycle.service)
                implementation(libs.androidx.palette)
                implementation(libs.androidx.paging.runtime.android)
                implementation(libs.androidx.splashscreen)
                implementation(libs.coil.gif)
                implementation(libs.koin.android)
            }
        }

        val skiaMain by creating {
            dependsOn(commonMain.get())
        }

        iosMain {
            dependsOn(skiaMain)
            dependencies {
                implementation(libs.androidx.paging.runtime.ios)
            }
        }

        val desktopMain by getting {
            dependsOn(skiaMain)
            dependencies {
                implementation(libs.appdirs)
            }
        }
    }
}

android {
    namespace = "fr.outadoc.justchatting.shared.ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    debugImplementation(libs.compose.ui.tooling)
}
