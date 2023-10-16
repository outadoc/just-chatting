package fr.outadoc.justchatting.feature.preferences.presentation

class NoopReadExternalDependenciesList : ReadExternalDependenciesList {
    override suspend fun invoke(): List<Dependency> = emptyList()
}
