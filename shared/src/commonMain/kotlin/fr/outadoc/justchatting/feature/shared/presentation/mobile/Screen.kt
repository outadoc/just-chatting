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

        @Serializable
        data object About : Settings {
            override val route: String
                get() = "settings/about"
        }

        @Serializable
        data object Appearance : Settings {
            override val route: String
                get() = "settings/appearance"
        }

        @Serializable
        data object DependencyCredits : Settings {
            override val route: String
                get() = "settings/dependency-credits"
        }

        @Serializable
        data object Notifications : Settings {
            override val route: String
                get() = "settings/notifications"
        }

        @Serializable
        data object ThirdParties : Settings {
            override val route: String
                get() = "settings/third-parties"
        }
    }
}

internal val DefaultScreen = Screen.Timeline
