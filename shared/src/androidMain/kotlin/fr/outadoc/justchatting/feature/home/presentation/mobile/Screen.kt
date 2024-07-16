package fr.outadoc.justchatting.feature.home.presentation.mobile

import kotlinx.serialization.Serializable

internal sealed interface Screen {
    @Serializable
    data object Live : Screen

    @Serializable
    data object Followed : Screen

    @Serializable
    sealed interface Settings : Screen {

        @Serializable
        data object Root : Settings

        @Serializable
        data object ThirdParties : Settings

        @Serializable
        data object About : Settings

        @Serializable
        data object Appearance : Settings

        @Serializable
        data object DependencyCredits : Settings
    }
}

internal val DefaultScreen = Screen.Live
