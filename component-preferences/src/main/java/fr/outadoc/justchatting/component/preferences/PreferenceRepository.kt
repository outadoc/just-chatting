package fr.outadoc.justchatting.component.preferences

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    val currentPreferences: Flow<AppPreferences>
    suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences)
}
