package fr.outadoc.justchatting.feature.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.utils.logging.logInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

internal class DataStorePreferenceRepository(
    private val dataStore: DataStore<Preferences>,
) : PreferenceRepository {
    private val defaultPreferences = AppPreferences()

    private val scope = CoroutineScope(SupervisorJob())

    override val currentPreferences: Flow<AppPreferences> =
        dataStore.data
            .map { prefs -> prefs.read() }
            .onEach { prefs ->
                logInfo<DataStorePreferenceRepository> { "Current prefs: $prefs" }
            }.shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
                replay = 1,
            )

    override suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences) {
        dataStore.edit { currentPreferences ->
            update(currentPreferences.read()).writeTo(currentPreferences)
        }
    }

    private fun Preferences.read(): AppPreferences {
        return AppPreferences(
            showTimestamps =
                this[CHAT_ACCESSIBILITY_TIMESTAMPS]
                    ?: defaultPreferences.showTimestamps,
            enableRecentMessages =
                this[THIRDPARTY_ENABLE_RECENT]
                    ?: defaultPreferences.enableRecentMessages,
            enableBttvEmotes =
                this[THIRDPARTY_ENABLE_BTTV]
                    ?: defaultPreferences.enableBttvEmotes,
            enableFfzEmotes =
                this[THIRDPARTY_ENABLE_FFZ]
                    ?: defaultPreferences.enableFfzEmotes,
            enableStvEmotes =
                this[THIRDPARTY_ENABLE_STV]
                    ?: defaultPreferences.enableStvEmotes,
            enablePronouns =
                this[THIRDPARTY_ENABLE_PRONOUNS]
                    ?: defaultPreferences.enablePronouns,
            enableNotifications =
                this[ENABLE_NOTIFICATIONS]
                    ?: defaultPreferences.enableNotifications,
            apiToken = this[USER_TOKEN]?.takeUnless { it.isBlank() },
        )
    }

    private fun AppPreferences.writeTo(prefs: MutablePreferences) {
        prefs[USER_TOKEN] = apiToken.orEmpty()

        prefs[CHAT_ACCESSIBILITY_TIMESTAMPS] = showTimestamps

        prefs[ENABLE_NOTIFICATIONS] = enableNotifications

        prefs[THIRDPARTY_ENABLE_RECENT] = enableRecentMessages
        prefs[THIRDPARTY_ENABLE_BTTV] = enableBttvEmotes
        prefs[THIRDPARTY_ENABLE_FFZ] = enableFfzEmotes
        prefs[THIRDPARTY_ENABLE_STV] = enableStvEmotes
        prefs[THIRDPARTY_ENABLE_PRONOUNS] = enablePronouns
    }

    private companion object {
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
