package fr.outadoc.justchatting.feature.shared.presentation.mobile

import kotlinx.serialization.Serializable

internal sealed interface Screen {

    val route: String

    @Serializable
    data object Followed : Screen {
        override val route: String
            get() = "followed"
    }

    @Serializable
    data object Timeline : Screen {
        override val route: String
            get() = "timeline"
    }

    @Serializable
    data object Search : Screen {
        override val route: String
            get() = "search"
    }

    @Serializable
    sealed interface Settings : Screen {

        @Serializable
        data object Root : Settings {
            override val route: String
                get() = "settings"
        }
    }
}

internal val DefaultScreen = Screen.Timeline
