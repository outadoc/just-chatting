package fr.outadoc.justchatting.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.AppUser
import fr.outadoc.justchatting.util.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharedPrefsPreferenceRepository(
    private val applicationContext: Context
) : PreferenceRepository {

    private val dataStore = applicationContext.dataStore

    override val helixClientId: Flow<String>
        get() = dataStore.data.map { prefs ->
            prefs[HELIX_CLIENT_ID]
                ?: applicationContext.getString(R.string.pref_default_apiToken)
        }

    override val helixRedirect: Flow<String>
        get() = dataStore.data.map { prefs ->
            prefs[HELIX_REDIRECT]
                ?: applicationContext.getString(R.string.pref_default_apiRedirect)
        }

    override val animateEmotes: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[CHAT_ANIMATED_EMOTES]
                ?: applicationContext.resources.getBoolean(R.bool.pref_default_chatEnableAnimatedEmotes)
        }

    override val showTimestamps: Flow<Boolean>
        get() = dataStore.data.map { prefs ->
            prefs[CHAT_TIMESTAMPS]
                ?: applicationContext.resources.getBoolean(R.bool.pref_default_chatEnableTimestamps)
        }

    override val recentMsgLimit: Flow<Int>
        get() = dataStore.data.map { prefs ->
            prefs[CHAT_RECENT_LIMIT]
                ?: applicationContext.resources.getInteger(R.integer.pref_default_recentChatLimit)
        }

    override val messageLimit: Flow<Int>
        get() = dataStore.data.map { prefs ->
            prefs[CHAT_LIMIT]
                ?: applicationContext.resources.getInteger(R.integer.pref_default_chatLimit)
        }

    override val appUser: Flow<AppUser>
        get() = dataStore.data.map { prefs ->
            val id = prefs[USER_ID]
            if (!id.isNullOrEmpty()) {
                val name = prefs[USER_LOGIN]
                val helixToken = prefs[USER_TOKEN]

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
