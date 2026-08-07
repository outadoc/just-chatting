package fr.outadoc.justchatting.feature.preferences.presentation

import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class DefaultReadExternalDependenciesList(
    private val dispatchersProvider: DispatchersProvider,
) : ReadExternalDependenciesList {
    override suspend fun invoke(): List<Dependency> = withContext(dispatchersProvider.io) {
        withContext(dispatchersProvider.io) {
            val deps: DependencyList =
                Json.decodeFromString(
                    Res.readBytes("files/dependencies.json").decodeToString(),
                )

            val extraDeps: DependencyList =
                Json.decodeFromString(
                    Res.readBytes("files/dependencies-extra.json").decodeToString(),
                )

            (extraDeps.dependencies + deps.dependencies)
                .sortedBy { dep -> dep.moduleName.lowercase() }
        }
    }
}
