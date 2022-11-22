import com.github.jk1.license.importer.XmlReportImporter
import com.github.jk1.license.render.JsonReportRenderer

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.licenseReport) apply false
}

group = "fr.outadoc"

