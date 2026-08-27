package fr.outadoc.justchatting.feature.shared.presentation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

public sealed interface Screen : NavKey {
    @Serializable
    public data object Followed : Screen

    @Serializable
    public data object Live : Screen

    @Serializable
    public data object Future : Screen

    @Serializable
    public data object Search : Screen

    @Serializable
    public data object Settings : Screen
}

public val DefaultScreen: Screen.Live = Screen.Live

/**
 * [SavedStateConfiguration] that registers all [Screen] subtypes as polymorphic [NavKey]
 * implementations. Required for [rememberNavBackStack] on non-Android platforms.
 */
public val ScreenNavBackStackConfig: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Screen.Followed::class, Screen.Followed.serializer())
                    subclass(Screen.Live::class, Screen.Live.serializer())
                    subclass(Screen.Future::class, Screen.Future.serializer())
                    subclass(Screen.Search::class, Screen.Search.serializer())
                    subclass(Screen.Settings::class, Screen.Settings.serializer())
                    subclass(DetailScreen.Chat::class, DetailScreen.Chat.serializer())
                    subclass(DetailScreen.About::class, DetailScreen.About.serializer())
                    subclass(DetailScreen.Appearance::class, DetailScreen.Appearance.serializer())
                    subclass(
                        DetailScreen.DependencyCredits::class,
                        DetailScreen.DependencyCredits.serializer(),
                    )
                    subclass(DetailScreen.Notifications::class, DetailScreen.Notifications.serializer())
                    subclass(DetailScreen.ThirdParties::class, DetailScreen.ThirdParties.serializer())
                }
            }
    }
