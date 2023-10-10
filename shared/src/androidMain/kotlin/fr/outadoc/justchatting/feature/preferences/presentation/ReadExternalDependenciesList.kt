package fr.outadoc.justchatting.feature.preferences.presentation

fun interface ReadExternalDependenciesList {
    suspend operator fun invoke(): List<Dependency>
}
