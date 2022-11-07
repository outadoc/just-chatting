package fr.outadoc.justchatting.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    val currentPreferences: Flow<AppPreferences>
    suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences)
}
