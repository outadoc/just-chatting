package fr.outadoc.justchatting.feature.shared.presentation.ui

import kotlinx.serialization.Serializable

internal sealed interface Screen {
    @Serializable
    data object Followed : Screen

    @Serializable
    data object Live : Screen

    @Serializable
    data object Future : Screen

    @Serializable
    data object Search : Screen

    @Serializable
    data object Settings : Screen
}

internal val DefaultScreen = Screen.Live
