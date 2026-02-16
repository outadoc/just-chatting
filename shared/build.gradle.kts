import com.github.jk1.license.render.JsonReportRenderer
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.licenseReport)
    alias(libs.plugins.sqldelight)
}

kotlin {
    explicitApi()

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
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries {
            framework {
                baseName = "JCShared"
                isStatic = true
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
                api(libs.kotlinx.coroutines)
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.serialization.json)
                api(libs.ktor.client.core)

                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.paging.common)
                implementation(libs.androidx.paging.compose.common)
                implementation(libs.coil.compose)
                implementation(libs.coil.core)
                implementation(libs.coil.ktor)
                implementation(libs.connectivity.core)
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
                implementation(libs.compose.navigation)
                implementation(libs.compose.runtime)
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.backhandler)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.contentNegociation)
                implementation(libs.ktor.logging)
                implementation(libs.ktor.serialization)
                implementation(libs.kmpalette.core)
                implementation(libs.kmpalette.extensions.network)
                implementation(libs.material.kolor)
                implementation(libs.okio)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.stately.common)
                implementation(libs.uri.kmp)
                implementation(libs.unicode.codepoints)
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
                implementation(libs.connectivity.android)
                implementation(libs.koin.android)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.okhttp)

                api(libs.sqldelight.driver.android)
            }
        }

        val skiaMain by creating {
            dependsOn(commonMain.get())
        }

        iosMain {
            dependsOn(skiaMain)
            dependencies {
                implementation(libs.androidx.paging.runtime.ios)
                implementation(libs.connectivity.apple)
                implementation(libs.ktor.client.darwin)

                api(libs.sqldelight.driver.native)
            }
        }

        val desktopMain by getting {
            dependsOn(skiaMain)
            dependencies {
                implementation(libs.appdirs)
                implementation(libs.connectivity.http)
                implementation(libs.ktor.client.java)
                implementation(libs.ktor.server.cors)
                implementation(libs.ktor.server.cio)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.logback)

                api(libs.sqldelight.driver.jvm)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        androidUnitTest {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "fr.outadoc.justchatting.shared"
}

android {
    namespace = "fr.outadoc.justchatting.shared"
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

licenseReport {
    outputDir = file("src/commonMain/composeResources/files").path
    configurations = arrayOf("releaseRuntimeClasspath")
    renderers = arrayOf(JsonReportRenderer("dependencies.json"))
}

tasks.named("generateLicenseReport") {
    outputs.upToDateWhen { false }
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
    debugImplementation(libs.compose.ui.tooling)
}
