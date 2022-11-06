package fr.outadoc.justchatting.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.outadoc.justchatting.model.AppUser
import fr.outadoc.justchatting.util.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharedPrefsPreferenceRepository(
    private val applicationContext: Context
) : PreferenceRepository {

    private val dataStore = applicationContext.dataStore
    private val defaultPreferences = AppPreferences()

    override val currentPreferences: Flow<AppPreferences>
        get() = dataStore.data.map { prefs ->
            AppPreferences(
                helixClientId = prefs[HELIX_CLIENT_ID] ?: defaultPreferences.helixClientId,
                helixRedirect = prefs[HELIX_REDIRECT] ?: defaultPreferences.helixRedirect,
                animateEmotes = prefs[CHAT_ANIMATED_EMOTES] ?: defaultPreferences.animateEmotes,
                showTimestamps = prefs[CHAT_TIMESTAMPS] ?: defaultPreferences.showTimestamps,
                recentMsgLimit = prefs[CHAT_RECENT_LIMIT] ?: defaultPreferences.recentMsgLimit,
                messageLimit = prefs[CHAT_LIMIT] ?: defaultPreferences.messageLimit,
                appUser = prefs.parseUser()
            )
        }

    override suspend fun updatePreferences(appPreferences: AppPreferences) {
        dataStore.edit { prefs ->
            prefs[HELIX_CLIENT_ID] = appPreferences.helixClientId
            prefs[HELIX_REDIRECT] = appPreferences.helixRedirect
            prefs[USER_ID] = appPreferences.appUser.id ?: ""
            prefs[USER_LOGIN] = appPreferences.appUser.login ?: ""
            prefs[USER_TOKEN] = appPreferences.appUser.helixToken ?: ""
            prefs[CHAT_ANIMATED_EMOTES] = appPreferences.animateEmotes
            prefs[CHAT_TIMESTAMPS] = appPreferences.showTimestamps
            prefs[CHAT_RECENT_LIMIT] = appPreferences.recentMsgLimit
            prefs[CHAT_LIMIT] = appPreferences.messageLimit
        }
    }

    override val helixClientId: Flow<String>
        get() = currentPreferences.map { prefs -> prefs.helixClientId }

    override val helixRedirect: Flow<String>
        get() = currentPreferences.map { prefs -> prefs.helixRedirect }

    override val animateEmotes: Flow<Boolean>
        get() = currentPreferences.map { prefs -> prefs.animateEmotes }

    override val showTimestamps: Flow<Boolean>
        get() = currentPreferences.map { prefs -> prefs.showTimestamps }

    override val recentMsgLimit: Flow<Int>
        get() = currentPreferences.map { prefs -> prefs.recentMsgLimit }

    override val messageLimit: Flow<Int>
        get() = currentPreferences.map { prefs -> prefs.messageLimit }

    override val appUser: Flow<AppUser>
        get() = currentPreferences.map { prefs -> prefs.appUser }

    private fun Preferences.parseUser(): AppUser {
        val id = this[USER_ID]
        return if (!id.isNullOrEmpty()) {
            val name = this[USER_LOGIN]
            val helixToken = this[USER_TOKEN]

            if (name.isNullOrEmpty() || helixToken.isNullOrEmpty()) {
                AppUser.NotValidated(id, name, helixToken)
            } else {
                AppUser.LoggedIn(id, name, helixToken)
            }
        } else {
            AppUser.NotLoggedIn
        }
    }

    override suspend fun updateUser(appUser: AppUser?) {
        dataStore.edit { prefs ->
            prefs[USER_ID] = appUser?.id ?: ""
            prefs[USER_LOGIN] = appUser?.login ?: ""
            prefs[USER_TOKEN] = appUser?.helixToken ?: ""
        }
    }

    private companion object {
        val HELIX_CLIENT_ID = stringPreferencesKey("helix_client_id")
        val HELIX_REDIRECT = stringPreferencesKey("helix_redirect")

        val USER_ID = stringPreferencesKey("user_id")
        val USER_LOGIN = stringPreferencesKey("username")
        val USER_TOKEN = stringPreferencesKey("token")

        val CHAT_ANIMATED_EMOTES = booleanPreferencesKey("animatedGifEmotes")
        val CHAT_TIMESTAMPS = booleanPreferencesKey("chat_timestamps")
        val CHAT_LIMIT = intPreferencesKey("chat_limit")
        val CHAT_RECENT_LIMIT = intPreferencesKey("chat_recent_limit")
    }
}
