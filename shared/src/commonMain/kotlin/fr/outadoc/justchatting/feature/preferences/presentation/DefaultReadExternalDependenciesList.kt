package fr.outadoc.justchatting.feature.preferences.presentation

import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.utils.core.DefaultJson
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext

internal class DefaultReadExternalDependenciesList(
    private val dispatchersProvider: DispatchersProvider,
) : ReadExternalDependenciesList {
    override suspend fun invoke(): List<Dependency> =
        withContext(dispatchersProvider.io) {
            // The dependency report format evolves with the plugin that generates it, so
            // ignore any keys we don't know about instead of crashing.
            val deps: DependencyList =
                DefaultJson.decodeFromString(
                    Res.readBytes("files/dependencies.json").decodeToString(),
                )

            val extraDeps: DependencyList =
                DefaultJson.decodeFromString(
                    Res.readBytes("files/dependencies-extra.json").decodeToString(),
                )

            (extraDeps.dependencies + deps.dependencies)
                .sortedBy { dep -> dep.moduleName.lowercase() }
        }
}
