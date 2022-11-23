package fr.outadoc.justchatting.oss

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class DependencyList(
    @SerializedName("dependencies")
    val dependencies: List<Dependency>
)
