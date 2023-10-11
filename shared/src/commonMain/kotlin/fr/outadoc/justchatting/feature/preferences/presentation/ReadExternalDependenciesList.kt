package fr.outadoc.justchatting.feature.preferences.presentation

interface ReadExternalDependenciesList {
    suspend operator fun invoke(): List<Dependency>
}
