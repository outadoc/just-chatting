package fr.outadoc.justchatting.feature.preferences.domain

import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import kotlinx.coroutines.flow.Flow

public interface PreferenceRepository {
    public val currentPreferences: Flow<AppPreferences>

    public suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences)
}
