package fr.outadoc.justchatting.feature.preferences.presentation

import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class AppleReadExternalDependenciesList : ReadExternalDependenciesList {

    override suspend fun invoke(): List<Dependency> = withContext(DispatchersProvider.io) {
        val deps: DependencyList =
            Json.decodeFromString(MR.files.dependencies_json.readText())

        val extraDeps: DependencyList =
            Json.decodeFromString(MR.files.dependencies_extra_json.readText())

        (extraDeps.dependencies + deps.dependencies)
            .sortedBy { dep -> dep.moduleName.lowercase() }
    }
}
