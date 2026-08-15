import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.ktlint)
}

kotlin {
    android {
        namespace = "fr.outadoc.justchatting.shared.internal"
        compileSdk = 37
        minSdk = 23

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        androidResources {
            enable = true
        }

        withHostTest {}

        optimization {
            consumerKeepRules.apply {
                publish = true
                file("consumer-rules.pro")
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

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
                implementation(libs.compose.components.resources)
                implementation(libs.compose.navigation3.ui)
                implementation(libs.compose.runtime)
                implementation(libs.connectivity.core)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.contentNegociation)
                implementation(libs.ktor.logging)
                implementation(libs.ktor.serialization)
                implementation(libs.okio)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.stately.common)
                implementation(libs.uri.kmp)
                implementation(libs.unicode.codepoints)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.browser)
                implementation(libs.androidx.emoji2.core)
                implementation(libs.androidx.paging.runtime.android)
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

        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "fr.outadoc.justchatting.shared.internal"
    publicResClass = true
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("fr.outadoc.justchatting.data.db")
        }
    }
}
