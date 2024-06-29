package fr.outadoc.justchatting.feature.preferences.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class SharedPrefsPreferenceRepository(
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
            showTimestamps = this[CHAT_ACCESSIBILITY_TIMESTAMPS]
                ?: defaultPreferences.showTimestamps,
            enableRecentMessages = this[THIRDPARTY_ENABLE_RECENT]
                ?: defaultPreferences.enableRecentMessages,
            enableBttvEmotes = this[THIRDPARTY_ENABLE_BTTV] ?: defaultPreferences.enableBttvEmotes,
            enableFfzEmotes = this[THIRDPARTY_ENABLE_FFZ] ?: defaultPreferences.enableFfzEmotes,
            enableStvEmotes = this[THIRDPARTY_ENABLE_STV] ?: defaultPreferences.enableStvEmotes,
            enablePronouns = this[THIRDPARTY_ENABLE_PRONOUNS] ?: defaultPreferences.enablePronouns,
            enableNotifications = this[ENABLE_NOTIFICATIONS] ?: defaultPreferences.enableNotifications,
            appUser = this.parseUser(),
        )
    }

    private fun AppPreferences.writeTo(prefs: MutablePreferences) {
        when (val user = appUser) {
            is AppUser.LoggedIn -> {
                prefs[USER_ID] = user.userId
                prefs[USER_LOGIN] = user.userLogin
                prefs[USER_TOKEN] = user.token
            }

            AppUser.NotLoggedIn -> {
                prefs[USER_ID] = ""
                prefs[USER_LOGIN] = ""
                prefs[USER_TOKEN] = ""
            }

            is AppUser.NotValidated -> {
                prefs[USER_ID] = ""
                prefs[USER_LOGIN] = ""
                prefs[USER_TOKEN] = user.token
            }
        }

        prefs[CHAT_ACCESSIBILITY_TIMESTAMPS] = showTimestamps

        prefs[ENABLE_NOTIFICATIONS] = enableNotifications

        prefs[THIRDPARTY_ENABLE_RECENT] = enableRecentMessages
        prefs[THIRDPARTY_ENABLE_BTTV] = enableBttvEmotes
        prefs[THIRDPARTY_ENABLE_FFZ] = enableFfzEmotes
        prefs[THIRDPARTY_ENABLE_STV] = enableStvEmotes
        prefs[THIRDPARTY_ENABLE_PRONOUNS] = enablePronouns
    }

    private fun Preferences.parseUser(): AppUser {
        val userId = this[USER_ID]
        val userLogin = this[USER_LOGIN]
        val token = this[USER_TOKEN]

        return if (token != null) {
            if (!userId.isNullOrEmpty() && !userLogin.isNullOrEmpty()) {
                AppUser.LoggedIn(
                    userId = userId,
                    userLogin = userLogin,
                    token = token,
                )
            } else {
                AppUser.NotValidated(
                    token = token,
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

        val CHAT_ACCESSIBILITY_TIMESTAMPS = booleanPreferencesKey("chat_timestamps")

        val ENABLE_NOTIFICATIONS = booleanPreferencesKey("notifications_enable")

        val THIRDPARTY_ENABLE_RECENT = booleanPreferencesKey("thirdparty_enable_recent")
        val THIRDPARTY_ENABLE_BTTV = booleanPreferencesKey("thirdparty_enable_bttv")
        val THIRDPARTY_ENABLE_FFZ = booleanPreferencesKey("thirdparty_enable_ffz")
        val THIRDPARTY_ENABLE_STV = booleanPreferencesKey("thirdparty_enable_stv")
        val THIRDPARTY_ENABLE_PRONOUNS = booleanPreferencesKey("thirdparty_enable_pronouns")
    }
}
