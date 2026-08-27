package fr.outadoc.justchatting.feature.shared.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

public sealed interface DetailScreen : NavKey {
    @Serializable
    public data class Chat(
        val id: String,
    ) : DetailScreen

    @Serializable
    public data object About : DetailScreen

    @Serializable
    public data object Appearance : DetailScreen

    @Serializable
    public data object DependencyCredits : DetailScreen

    @Serializable
    public data object Notifications : DetailScreen

    @Serializable
    public data object ThirdParties : DetailScreen
}
