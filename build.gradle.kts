plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.licenseReport) apply false
    alias(libs.plugins.sqldelight) apply false

    alias(libs.plugins.spotless)
}

group = "fr.outadoc"

spotless {
    kotlin {
        target("**/*.kt", "**/*.kts")
        ktlint("1.8.0")
            .editorConfigOverride(
                mapOf(
                    "max_line_length" to "off",
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                    "ktlint_standard_property-naming" to "disabled",
                    "ktlint_standard_function-expression-body" to "disabled",
                ),
            )
        endWithNewline()
    }

    json {
        target("**/*.json")
        simple().indentWithSpaces(2)
    }
}
