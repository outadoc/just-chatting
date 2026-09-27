package fr.outadoc.justchatting.feature.shared.presentation

import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.presentation.ReadExternalDependenciesList
import fr.outadoc.justchatting.utils.logging.LogStrategy
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * App-wide dependencies for Swift. Screen-level ViewModels are created through [ViewModelOwner]
 * instead, so that they are cleared along with their view.
 */
public class KoinHelper : KoinComponent {
    public fun getMainRouterViewModel(): MainRouterViewModel = get()

    public fun getPreferenceRepository(): PreferenceRepository = get()

    public fun getReadExternalDependenciesList(): ReadExternalDependenciesList = get()

    public fun getDeeplinkReceiver(): DeeplinkReceiver = get()

    public fun getLogStrategy(): LogStrategy = get()
}
