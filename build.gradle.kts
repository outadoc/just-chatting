plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.licenseReport) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.skie) apply false

    alias(libs.plugins.spotless)
}

group = "fr.outadoc"

spotless {
    kotlin {
        target("**/*.kt", "**/*.kts")
        ktlint("0.48.2").userData(mapOf("disabled_rules" to "no-wildcard-imports"))
        endWithNewline()
    }

    json {
        target("**/*.json")
        simple().indentWithSpaces(2)
    }
}
