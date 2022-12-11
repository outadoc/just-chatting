package fr.outadoc.justchatting.repository

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.outadoc.justchatting.component.preferences.PreferenceRepository
import fr.outadoc.justchatting.component.preferences.AppUser
import fr.outadoc.justchatting.util.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharedPrefsPreferenceRepository(
    applicationContext: Context
) : fr.outadoc.justchatting.component.preferences.PreferenceRepository {

    private val dataStore = applicationContext.dataStore
    private val defaultPreferences = fr.outadoc.justchatting.component.preferences.AppPreferences()

    override val currentPreferences: Flow<fr.outadoc.justchatting.component.preferences.AppPreferences>
        get() = dataStore.data.map { prefs -> prefs.read() }

    override suspend fun updatePreferences(update: (fr.outadoc.justchatting.component.preferences.AppPreferences) -> fr.outadoc.justchatting.component.preferences.AppPreferences) {
        dataStore.edit { currentPreferences ->
            update(currentPreferences.read()).writeTo(currentPreferences)
        }
    }

    private fun Preferences.read(): fr.outadoc.justchatting.component.preferences.AppPreferences {
        return fr.outadoc.justchatting.component.preferences.AppPreferences(
            animateEmotes = this[CHAT_ANIMATED_EMOTES] ?: defaultPreferences.animateEmotes,
            showTimestamps = this[CHAT_TIMESTAMPS] ?: defaultPreferences.showTimestamps,
            recentMsgLimit = this[CHAT_RECENT_LIMIT] ?: defaultPreferences.recentMsgLimit,
            messageLimit = this[CHAT_LIMIT] ?: defaultPreferences.messageLimit,
            appUser = this.parseUser()
        )
    }

    private fun fr.outadoc.justchatting.component.preferences.AppPreferences.writeTo(prefs: MutablePreferences) {
        prefs[USER_ID] = appUser.id ?: ""
        prefs[USER_LOGIN] = appUser.login ?: ""
        prefs[USER_TOKEN] = appUser.helixToken ?: ""
        prefs[CHAT_ANIMATED_EMOTES] = animateEmotes
        prefs[CHAT_TIMESTAMPS] = showTimestamps
        prefs[CHAT_RECENT_LIMIT] = recentMsgLimit
        prefs[CHAT_LIMIT] = messageLimit
    }

    private fun Preferences.parseUser(): fr.outadoc.justchatting.component.preferences.AppUser {
        val id = this[USER_ID]
        val login = this[USER_LOGIN]
        val helixToken = this[USER_TOKEN]

        return if (helixToken != null) {
            if (!id.isNullOrEmpty() && !login.isNullOrEmpty()) {
                fr.outadoc.justchatting.component.preferences.AppUser.LoggedIn(
                    id = id,
                    login = login,
                    helixToken = helixToken
                )
            } else {
                fr.outadoc.justchatting.component.preferences.AppUser.NotValidated(
                    helixToken = helixToken
                )
            }
        } else {
            fr.outadoc.justchatting.component.preferences.AppUser.NotLoggedIn
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
