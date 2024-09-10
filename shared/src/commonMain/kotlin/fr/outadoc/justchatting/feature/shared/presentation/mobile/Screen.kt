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
    sealed interface Settings : Screen {

        @Serializable
        data object Root : Settings

        @Serializable
        data object About : Settings

        @Serializable
        data object Appearance : Settings

        @Serializable
        data object DependencyCredits : Settings

        @Serializable
        data object Notifications : Settings

        @Serializable
        data object ThirdParties : Settings
    }
}

internal val DefaultScreen = Screen.Timeline
