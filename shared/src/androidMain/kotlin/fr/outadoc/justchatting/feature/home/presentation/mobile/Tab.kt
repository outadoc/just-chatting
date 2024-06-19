package fr.outadoc.justchatting.feature.home.presentation.mobile

import kotlinx.serialization.Serializable

sealed interface Tab {
    @Serializable
    data object Live : Tab

    @Serializable
    data object Followed : Tab

    @Serializable
    data object Settings : Tab
}

val DefaultTab = Tab.Live
