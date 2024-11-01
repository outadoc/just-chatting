package fr.outadoc.justchatting.feature.preferences.presentation

import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

internal class DefaultReadExternalDependenciesList : ReadExternalDependenciesList {

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun invoke(): List<Dependency> = withContext(DispatchersProvider.io) {
        withContext(DispatchersProvider.io) {
            val deps: DependencyList = Json.decodeFromString(
                Res.readBytes("files/dependencies.json").decodeToString(),
            )

            val extraDeps: DependencyList = Json.decodeFromString(
                Res.readBytes("files/dependencies-extra.json").decodeToString(),
            )

            (extraDeps.dependencies + deps.dependencies)
                .sortedBy { dep -> dep.moduleName.lowercase() }
        }
    }
}
