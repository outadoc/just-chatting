package fr.outadoc.justchatting.feature.preferences.presentation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DependencyList(
    @SerialName("dependencies")
    val dependencies: List<Dependency>
)
