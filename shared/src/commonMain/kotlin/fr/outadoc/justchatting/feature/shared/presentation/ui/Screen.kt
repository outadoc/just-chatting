package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal sealed interface Screen : NavKey {
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

/**
 * [SavedStateConfiguration] that registers all [Screen] subtypes as polymorphic [NavKey]
 * implementations. Required for [rememberNavBackStack] on non-Android platforms.
 */
internal val ScreenNavBackStackConfig: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Screen.Followed::class, Screen.Followed.serializer())
                    subclass(Screen.Live::class, Screen.Live.serializer())
                    subclass(Screen.Future::class, Screen.Future.serializer())
                    subclass(Screen.Search::class, Screen.Search.serializer())
                    subclass(Screen.Settings::class, Screen.Settings.serializer())
                }
            }
    }
