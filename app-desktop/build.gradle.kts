import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    alias(libs.plugins.compose.multiplatform)
}

compose.desktop {
    application {
        mainClass = "fr.outadoc.justchatting.Main"

        nativeDistributions {
            packageName = "Just Chatting"
            packageVersion = "1.0.0"

            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
            )

            windows {
                menu = true
                upgradeUuid = "4C55AF4A-F39A-4E6F-B753-DE647A8F8BE2"
            }

            modules(
                "java.net.http",
                "java.sql",
                "jdk.unsupported",
            )
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(platform(libs.kotlin.bom))
    implementation(compose.desktop.currentOs)
}
