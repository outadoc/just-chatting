package fr.outadoc.justchatting.feature.shared.presentation.mobile

import kotlinx.serialization.Serializable

internal sealed interface Screen {

    @Serializable
    data object Followed : Screen

    @Serializable
    data object Timeline : Screen

    @Serializable
    data object Search : Screen

    @Serializable
    data object Settings : Screen
}

internal val DefaultScreen = Screen.Timeline
