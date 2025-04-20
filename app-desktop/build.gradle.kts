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
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                upgradeUuid = "61DAB35E-17CB-43B0-81D5-B30E1C0830FA"
            }
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(platform(libs.kotlin.bom))
    implementation(compose.desktop.currentOs)
}
