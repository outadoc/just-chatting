package fr.outadoc.justchatting.repository

import fr.outadoc.justchatting.model.AppUser
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository :
    AuthPreferencesRepository,
    ChatPreferencesRepository,
    UserPreferencesRepository {

    val currentPreferences: Flow<AppPreferences>
    suspend fun updatePreferences(appPreferences: AppPreferences)
}

data class AppPreferences(
    val helixClientId: String,
    val helixRedirect: String,
    val animateEmotes: Boolean,
    val showTimestamps: Boolean,
    val recentMsgLimit: Int,
    val messageLimit: Int,
    val appUser: AppUser
)
