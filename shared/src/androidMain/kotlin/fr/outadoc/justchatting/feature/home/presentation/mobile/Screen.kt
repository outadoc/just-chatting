package fr.outadoc.justchatting.feature.home.presentation.mobile

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Live : Screen

    @Serializable
    data object Followed : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object DependencyCredits : Screen
}

val DefaultScreen = Screen.Live
