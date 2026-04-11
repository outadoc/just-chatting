package fr.outadoc.justchatting.feature.preferences.presentation

public class NoopReadExternalDependenciesList : ReadExternalDependenciesList {
    override suspend fun invoke(): List<Dependency> = emptyList()
}
