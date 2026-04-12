package fr.outadoc.justchatting.feature.preferences.presentation

internal class NoopReadExternalDependenciesList : ReadExternalDependenciesList {
    override suspend fun invoke(): List<Dependency> = emptyList()
}
