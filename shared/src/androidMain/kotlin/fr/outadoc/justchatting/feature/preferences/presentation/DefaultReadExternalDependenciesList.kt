package fr.outadoc.justchatting.feature.preferences.presentation

import android.content.Context
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

internal class DefaultReadExternalDependenciesList(
    private val context: Context,
) : ReadExternalDependenciesList {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend operator fun invoke(): List<Dependency> =
        withContext(DispatchersProvider.io) {
            val deps: DependencyList =
                Json.decodeFromStream(context.assets.open("dependencies.json"))

            val extraDeps: DependencyList =
                Json.decodeFromStream(context.assets.open("dependencies-extra.json"))

            (extraDeps.dependencies + deps.dependencies)
                .sortedBy { dep -> dep.moduleName.lowercase() }
        }
}
