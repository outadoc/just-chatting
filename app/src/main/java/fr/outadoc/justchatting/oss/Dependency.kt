package fr.outadoc.justchatting.oss

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Dependency(
    @SerializedName("moduleName")
    val moduleName: String,
    @SerializedName("moduleUrl")
    val moduleUrl: String? = null,
    @SerializedName("moduleVersion")
    val moduleVersion: String? = null,
    @SerializedName("moduleLicense")
    val moduleLicense: String? = null,
    @SerializedName("moduleLicenseUrl")
    val moduleLicenseUrl: String? = null
)
