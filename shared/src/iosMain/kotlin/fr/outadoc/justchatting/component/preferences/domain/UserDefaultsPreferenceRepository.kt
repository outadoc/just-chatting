package fr.outadoc.justchatting.component.preferences.domain

import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.utils.logging.logInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.Foundation.NSUserDefaults

internal class UserDefaultsPreferenceRepository : PreferenceRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val defaultPreferences = AppPreferences()

    private val _currentPreferences = MutableStateFlow(userDefaults.read())
    override val currentPreferences: Flow<AppPreferences> = _currentPreferences

    private val mutex = Mutex()

    override suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences) {
        mutex.withLock {
            val oldDefaults = userDefaults.read()
            val afterEdit = update(oldDefaults)
            afterEdit.writeToDefaults()
            _currentPreferences.value = afterEdit
        }
    }

    private fun NSUserDefaults.read(): AppPreferences {
        return AppPreferences(
            showTimestamps = boolForKeyOrNull(CHAT_ACCESSIBILITY_TIMESTAMPS)
                ?: defaultPreferences.showTimestamps,
            enableRecentMessages = boolForKeyOrNull(THIRDPARTY_ENABLE_RECENT)
                ?: defaultPreferences.enableRecentMessages,
            enableBttvEmotes = boolForKeyOrNull(THIRDPARTY_ENABLE_BTTV)
                ?: defaultPreferences.enableBttvEmotes,
            enableFfzEmotes = boolForKeyOrNull(THIRDPARTY_ENABLE_FFZ)
                ?: defaultPreferences.enableFfzEmotes,
            enableStvEmotes = boolForKeyOrNull(THIRDPARTY_ENABLE_STV)
                ?: defaultPreferences.enableStvEmotes,
            enablePronouns = boolForKeyOrNull(THIRDPARTY_ENABLE_PRONOUNS)
                ?: defaultPreferences.enablePronouns,
            enableNotifications = boolForKeyOrNull(ENABLE_NOTIFICATIONS)
                ?: defaultPreferences.enableNotifications,
            apiToken = stringForKey(USER_TOKEN)
                ?: defaultPreferences.apiToken,
        )
    }

    private fun NSUserDefaults.hasKey(key: String): Boolean {
        return objectForKey(key) != null
    }

    private fun NSUserDefaults.boolForKeyOrNull(key: String): Boolean? {
        return if (hasKey(key)) {
            boolForKey(key)
        } else {
            null
        }
    }

    private fun AppPreferences.writeToDefaults() = with(userDefaults) {
        logInfo<UserDefaultsPreferenceRepository> {
            "Writing preferences to NSUserDefaults: ${this@writeToDefaults}"
        }

        setObject(apiToken, forKey = USER_TOKEN)

        setObject(showTimestamps, forKey = CHAT_ACCESSIBILITY_TIMESTAMPS)

        setObject(enableNotifications, forKey = ENABLE_NOTIFICATIONS)

        setObject(enableRecentMessages, forKey = THIRDPARTY_ENABLE_RECENT)
        setObject(enableBttvEmotes, forKey = THIRDPARTY_ENABLE_BTTV)
        setObject(enableFfzEmotes, forKey = THIRDPARTY_ENABLE_FFZ)
        setObject(enableStvEmotes, forKey = THIRDPARTY_ENABLE_STV)
        setObject(enablePronouns, forKey = THIRDPARTY_ENABLE_PRONOUNS)
    }

    private companion object {
        const val USER_TOKEN = "token"

        const val CHAT_ACCESSIBILITY_TIMESTAMPS = "chat_timestamps"

        const val ENABLE_NOTIFICATIONS = "notifications_enable"

        const val THIRDPARTY_ENABLE_RECENT = "thirdparty_enable_recent"
        const val THIRDPARTY_ENABLE_BTTV = "thirdparty_enable_bttv"
        const val THIRDPARTY_ENABLE_FFZ = "thirdparty_enable_ffz"
        const val THIRDPARTY_ENABLE_STV = "thirdparty_enable_stv"
        const val THIRDPARTY_ENABLE_PRONOUNS = "thirdparty_enable_pronouns"
    }
}
