package fr.outadoc.justchatting.feature.preferences.presentation

import android.content.Context
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class AndroidReadExternalDependenciesList(
    private val context: Context,
) : ReadExternalDependenciesList {

    override suspend operator fun invoke(): List<Dependency> =
        withContext(DispatchersProvider.io) {
            val deps: DependencyList =
                Json.decodeFromString(MR.files.dependencies_json.readText(context))

            val extraDeps: DependencyList =
                Json.decodeFromString(MR.files.dependencies_extra_json.readText(context))

            (extraDeps.dependencies + deps.dependencies)
                .sortedBy { dep -> dep.moduleName.lowercase() }
        }
}
