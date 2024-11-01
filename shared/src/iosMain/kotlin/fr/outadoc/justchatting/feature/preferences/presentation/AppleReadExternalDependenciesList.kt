package fr.outadoc.justchatting.feature.preferences.presentation

import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.foundation.NSInputStream

internal class AppleReadExternalDependenciesList : ReadExternalDependenciesList {

    @OptIn(ExperimentalResourceApi::class, ExperimentalSerializationApi::class)
    override suspend fun invoke(): List<Dependency> = withContext(DispatchersProvider.io) {
        withContext(DispatchersProvider.io) {
            val deps: DependencyList = Json.decodeFromSource(
                Res.readBytes("files/dependencies.json").inputStream().asSource().buffered(),
            )

            val extraDeps: DependencyList = Json.decodeFromSource(
                Res.readBytes("files/dependencies-extra.json").inputStream().asSource().buffered(),
            )

            (extraDeps.dependencies + deps.dependencies)
                .sortedBy { dep -> dep.moduleName.lowercase() }
        }
    }

    private fun ByteArray.inputStream(): NSInputStream {
        return NSInputStream(data = toNSData())
    }
}
