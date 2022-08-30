plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(libs.javax.jaxb.api)
    implementation(libs.kotlinpoet)
    implementation(libs.rxjava.core)
    implementation(libs.rxjava.rxkotlin)
    implementation(libs.slf4j)

    testImplementation(libs.junit)
    testImplementation(libs.logback)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint("0.45.2").userData(mapOf("disabled_rules" to "no-wildcard-imports"))
        licenseHeaderFile("spotless.header.kts")
        endWithNewline()
    }
}
