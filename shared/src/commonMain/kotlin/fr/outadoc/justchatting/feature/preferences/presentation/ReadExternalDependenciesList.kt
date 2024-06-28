package fr.outadoc.justchatting.feature.preferences.presentation

internal interface ReadExternalDependenciesList {
    suspend operator fun invoke(): List<Dependency>
}
