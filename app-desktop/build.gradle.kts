import dev.nucleusframework.desktop.application.dsl.AppImageCategory
import dev.nucleusframework.desktop.application.dsl.TargetFormat
import java.io.FileOutputStream

plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.nucleus)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

sourceSets {
    main {
        resources.srcDir(layout.buildDirectory.file("generated/lib_version"))
    }
}

nucleus.application {
    mainClass = "fr.outadoc.justchatting.Main"

    buildTypes {
        release {
            proguard {
                configurationFiles.from(project.file("compose-desktop.pro"))
            }
        }
    }

    nativeDistributions {
        val versionCode = findProperty("externalVersionCode") as String?
        val versionName = findProperty("externalVersionName") as String?

        appName = "Just Chatting"
        packageName = "justchatting"
        packageVersion = versionName ?: "1.0.0"
        description = "An app focused on a great Twitch chat experience"
        homepage = "https://github.com/outadoc/just-chatting"

        targetFormats(
            TargetFormat.Dmg,
            TargetFormat.Msi,
            TargetFormat.AppImage,
            TargetFormat.Flatpak,
        )

        windows {
            menu = true
            upgradeUuid = "4C55AF4A-F39A-4E6F-B753-DE647A8F8BE2"
            iconFile = project.file("assets/icon_windows.ico")

            // MSI requires MAJOR.MINOR.BUILD with MAJOR/MINOR <= 255 and BUILD <= 65535,
            // which the date-based packageVersion above can't satisfy.
            msiPackageVersion = versionCode?.let { "1.0.$it" } ?: "1.0.0"
        }

        macOS {
            iconFile = project.file("assets/icon_macos.icns")
            bundleID = "fr.outadoc.justchatting"
        }

        linux {
            appCategory = "Chat"
            iconFile = project.file("assets/icon_linux.svg")

            appImage {
                category = AppImageCategory.Network
            }

            flatpak {
                runtime = "org.freedesktop.Platform"
                runtimeVersion = "25.08"
                sdk = "org.freedesktop.Sdk"

                finishArgs =
                    listOf(
                        "--share=ipc",
                        "--socket=x11",
                        "--socket=wayland",
                        "--device=dri",
                        "--share=network",
                    )
            }
        }

        modules(
            "java.net.http",
            "java.sql",
            "jdk.unsupported",
        )
    }
}

dependencies {
    implementation(project(":shared-ui"))
    implementation(platform(libs.kotlin.bom))
    implementation(compose.desktop.currentOs)
    implementation(libs.nucleus.core.runtime)
}

tasks.register("generateVersionProperties") {
    group = "Build"
    description = "Generate file containing the app version"

    doLast {
        val propertiesFile =
            file("$buildDir/generated/lib_version/version.txt").apply {
                parentFile.mkdirs()
            }

        val version = findProperty("externalVersionName") as String?

        FileOutputStream(propertiesFile)
            .bufferedWriter()
            .use { bw ->
                if (version != null) {
                    bw.appendLine(version)
                }
            }
    }
}

tasks.named("processResources") {
    dependsOn("generateVersionProperties")
}
