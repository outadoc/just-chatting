package fr.outadoc.justchatting.feature.preferences.presentation

import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.compose.resources.ExperimentalResourceApi

internal class AndroidReadExternalDependenciesList : ReadExternalDependenciesList {

    @OptIn(ExperimentalResourceApi::class, ExperimentalSerializationApi::class)
    override suspend operator fun invoke(): List<Dependency> =
        withContext(DispatchersProvider.io) {
            val deps: DependencyList = Json.decodeFromStream(
                Res.readBytes("files/dependencies.json").inputStream()
            )

            val extraDeps: DependencyList = Json.decodeFromStream(
                Res.readBytes("files/dependencies-extra.json").inputStream()
            )

            (extraDeps.dependencies + deps.dependencies)
                .sortedBy { dep -> dep.moduleName.lowercase() }
        }
}
