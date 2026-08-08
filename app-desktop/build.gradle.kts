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

        packageName = "Just Chatting"
        packageVersion = versionCode?.let { "1.0.$versionCode" } ?: "1.0.0"
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
        }

        macOS {
            iconFile = project.file("assets/icon_macos.icns")
            bundleID = "fr.outadoc.justchatting"
        }

        linux {
            appCategory = "Chat"
            iconFile = project.file("assets/icon_linux.svg")
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
