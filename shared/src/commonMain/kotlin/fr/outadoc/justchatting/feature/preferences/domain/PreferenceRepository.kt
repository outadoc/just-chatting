package fr.outadoc.justchatting.feature.preferences.domain

import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import kotlinx.coroutines.flow.Flow

internal interface PreferenceRepository {
    val currentPreferences: Flow<AppPreferences>

    suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences)
}
