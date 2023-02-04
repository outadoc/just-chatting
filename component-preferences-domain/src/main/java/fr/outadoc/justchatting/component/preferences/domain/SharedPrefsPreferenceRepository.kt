package fr.outadoc.justchatting.component.preferences.domain

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.component.preferences.data.AppUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SharedPrefsPreferenceRepository(
    applicationContext: Context,
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
            enableRecentMessages = this[THIRDPARTY_ENABLE_RECENT]
                ?: defaultPreferences.enableRecentMessages,
            enableBttvEmotes = this[THIRDPARTY_ENABLE_BTTV] ?: defaultPreferences.enableBttvEmotes,
            enableFfzEmotes = this[THIRDPARTY_ENABLE_FFZ] ?: defaultPreferences.enableFfzEmotes,
            enableStvEmotes = this[THIRDPARTY_ENABLE_STV] ?: defaultPreferences.enableStvEmotes,
            appUser = this.parseUser(),
        )
    }

    private fun AppPreferences.writeTo(prefs: MutablePreferences) {
        prefs[USER_ID] = appUser.id ?: ""
        prefs[USER_LOGIN] = appUser.login ?: ""
        prefs[USER_TOKEN] = appUser.helixToken ?: ""

        prefs[CHAT_ANIMATED_EMOTES] = animateEmotes
        prefs[CHAT_TIMESTAMPS] = showTimestamps

        prefs[THIRDPARTY_ENABLE_RECENT] = enableRecentMessages
        prefs[THIRDPARTY_ENABLE_BTTV] = enableBttvEmotes
        prefs[THIRDPARTY_ENABLE_FFZ] = enableFfzEmotes
        prefs[THIRDPARTY_ENABLE_STV] = enableStvEmotes
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
                    helixToken = helixToken,
                )
            } else {
                AppUser.NotValidated(
                    helixToken = helixToken,
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

        val THIRDPARTY_ENABLE_RECENT = booleanPreferencesKey("thirdparty_enable_recent")
        val THIRDPARTY_ENABLE_BTTV = booleanPreferencesKey("thirdparty_enable_bttv")
        val THIRDPARTY_ENABLE_FFZ = booleanPreferencesKey("thirdparty_enable_ffz")
        val THIRDPARTY_ENABLE_STV = booleanPreferencesKey("thirdparty_enable_stv")
    }
}
