package fr.outadoc.justchatting.feature.settings.presentation

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class DependencyList(
    @SerializedName("dependencies")
    val dependencies: List<Dependency>
)
