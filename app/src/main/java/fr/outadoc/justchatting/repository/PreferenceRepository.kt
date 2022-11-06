package fr.outadoc.justchatting.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository :
    AuthPreferencesRepository,
    ChatPreferencesRepository,
    UserPreferencesRepository {

    val currentPreferences: Flow<AppPreferences>
    suspend fun updatePreferences(appPreferences: AppPreferences)
}
