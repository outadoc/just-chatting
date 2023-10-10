package fr.outadoc.justchatting.component.preferences.domain

import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    val currentPreferences: Flow<AppPreferences>
    suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences)
}
