package fr.outadoc.justchatting.feature.shared.presentation.mobile

import kotlinx.serialization.Serializable

internal sealed interface DetailScreen {

    @Serializable
    data class Chat(val id: String) : DetailScreen

    @Serializable
    data object About : DetailScreen

    @Serializable
    data object Appearance : DetailScreen

    @Serializable
    data object DependencyCredits : DetailScreen

    @Serializable
    data object Notifications : DetailScreen

    @Serializable
    data object ThirdParties : DetailScreen
}
