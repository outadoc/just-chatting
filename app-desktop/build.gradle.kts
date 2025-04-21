import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.FileOutputStream

plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    alias(libs.plugins.compose.multiplatform)
}

sourceSets {
    main {
        resources.srcDir("$buildDir/generated/lib_version")
    }
}

compose.desktop {
    application {
        mainClass = "fr.outadoc.justchatting.Main"

        nativeDistributions {
            val versionCode = findProperty("externalVersionCode") as String?

            packageName = "Just Chatting"
            packageVersion = versionCode?.let { "1.0.$versionCode" } ?: "1.0.0"

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

tasks.register("generateVersionProperties") {
    doLast {
        val propertiesFile = file("$buildDir/generated/lib_version/version.txt").apply {
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
