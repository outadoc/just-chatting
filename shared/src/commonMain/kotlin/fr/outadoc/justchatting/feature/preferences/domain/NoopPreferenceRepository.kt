package fr.outadoc.justchatting.feature.preferences.domain

import fr.outadoc.justchatting.feature.preferences.data.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class NoopPreferenceRepository : PreferenceRepository {

    override val currentPreferences: Flow<AppPreferences> = flowOf(AppPreferences())

    override suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences) {}
}
