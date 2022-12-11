package fr.outadoc.justchatting.repository

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.outadoc.justchatting.component.twitch.model.AppUser
import fr.outadoc.justchatting.util.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharedPrefsPreferenceRepository(
    applicationContext: Context
) : PreferenceRepository {

    private val dataStore = applicationContext.dataStore
    private val defaultPreferences = AppPreferences()

    override val currentPreferences: Flow<AppPreferences>
        get() = dataStore.data.map { prefs -> prefs.read() }

    override suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences) {
        dataStore.edit { currentPreferences ->
            update(currentPreferences.read()).writeTo(currentPreferences)
        }
    }

    private fun Preferences.read(): AppPreferences {
        return AppPreferences(
            animateEmotes = this[CHAT_ANIMATED_EMOTES] ?: defaultPreferences.animateEmotes,
            showTimestamps = this[CHAT_TIMESTAMPS] ?: defaultPreferences.showTimestamps,
            recentMsgLimit = this[CHAT_RECENT_LIMIT] ?: defaultPreferences.recentMsgLimit,
            messageLimit = this[CHAT_LIMIT] ?: defaultPreferences.messageLimit,
            appUser = this.parseUser()
        )
    }

    private fun AppPreferences.writeTo(prefs: MutablePreferences) {
        prefs[USER_ID] = appUser.id ?: ""
        prefs[USER_LOGIN] = appUser.login ?: ""
        prefs[USER_TOKEN] = appUser.helixToken ?: ""
        prefs[CHAT_ANIMATED_EMOTES] = animateEmotes
        prefs[CHAT_TIMESTAMPS] = showTimestamps
        prefs[CHAT_RECENT_LIMIT] = recentMsgLimit
        prefs[CHAT_LIMIT] = messageLimit
    }

    private fun Preferences.parseUser(): AppUser {
        val id = this[USER_ID]
        val login = this[USER_LOGIN]
        val helixToken = this[USER_TOKEN]

        return if (helixToken != null) {
            if (!id.isNullOrEmpty() && !login.isNullOrEmpty()) {
                AppUser.LoggedIn(
                    id = id,
                    login = login,
                    helixToken = helixToken
                )
            } else {
                AppUser.NotValidated(
                    helixToken = helixToken
                )
            }
        } else {
            AppUser.NotLoggedIn
        }
    }

    private companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_LOGIN = stringPreferencesKey("username")
        val USER_TOKEN = stringPreferencesKey("token")

        val CHAT_ANIMATED_EMOTES = booleanPreferencesKey("animatedGifEmotes")
        val CHAT_TIMESTAMPS = booleanPreferencesKey("chat_timestamps")
        val CHAT_LIMIT = intPreferencesKey("chat_limit")
        val CHAT_RECENT_LIMIT = intPreferencesKey("chat_recent_limit")
    }
}
