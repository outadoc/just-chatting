package fr.outadoc.justchatting.feature.preferences.presentation

public interface ReadExternalDependenciesList {
    public suspend operator fun invoke(): List<Dependency>
}
