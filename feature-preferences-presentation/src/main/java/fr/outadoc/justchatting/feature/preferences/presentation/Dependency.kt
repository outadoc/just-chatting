package fr.outadoc.justchatting.feature.preferences.presentation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dependency(
    @SerialName("moduleName")
    val moduleName: String,
    @SerialName("moduleUrl")
    val moduleUrl: String? = null,
    @SerialName("moduleVersion")
    val moduleVersion: String? = null,
    @SerialName("moduleLicense")
    val moduleLicense: String? = null,
    @SerialName("moduleLicenseUrl")
    val moduleLicenseUrl: String? = null
)
