package fr.outadoc.justchatting.component.preferences.domain

import fr.outadoc.justchatting.component.preferences.data.AppPreferences
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDefaultsDidChangeNotification

class UserDefaultsPreferenceRepository : PreferenceRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val defaultPreferences = AppPreferences()

    override val currentPreferences: Flow<AppPreferences> =
        callbackFlow {
            val observer = NSNotificationCenter.defaultCenter
                .addObserverForName(
                    name = NSUserDefaultsDidChangeNotification,
                    `object` = userDefaults,
                    queue = null,
                ) { _ ->
                    trySend(userDefaults.read())
                }

            awaitClose { NSNotificationCenter.defaultCenter.removeObserver(observer) }
        }
            .flowOn(DispatchersProvider.default)
            .distinctUntilChanged()

    override suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences) {
        update(userDefaults.read()).writeTo(userDefaults)
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
            appUser = this.parseUser(),
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

    private fun AppPreferences.writeTo(prefs: NSUserDefaults) = with(prefs) {
        when (val user = appUser) {
            is AppUser.LoggedIn -> {
                setObject(user.userId, forKey = USER_ID)
                setObject(user.userLogin, forKey = USER_LOGIN)
                setObject(user.token, forKey = USER_TOKEN)
            }

            AppUser.NotLoggedIn -> {
                setObject("", forKey = USER_ID)
                setObject("", forKey = USER_LOGIN)
                setObject("", forKey = USER_TOKEN)
            }

            is AppUser.NotValidated -> {
                setObject("", forKey = USER_ID)
                setObject("", forKey = USER_LOGIN)
                setObject(user.token, forKey = USER_TOKEN)
            }
        }

        setObject(showTimestamps, forKey = CHAT_ACCESSIBILITY_TIMESTAMPS)

        setObject(enableNotifications, forKey = ENABLE_NOTIFICATIONS)

        setObject(enableRecentMessages, forKey = THIRDPARTY_ENABLE_RECENT)
        setObject(enableBttvEmotes, forKey = THIRDPARTY_ENABLE_BTTV)
        setObject(enableFfzEmotes, forKey = THIRDPARTY_ENABLE_FFZ)
        setObject(enableStvEmotes, forKey = THIRDPARTY_ENABLE_STV)
        setObject(enablePronouns, forKey = THIRDPARTY_ENABLE_PRONOUNS)
    }

    private fun NSUserDefaults.parseUser(): AppUser {
        val userId = stringForKey(USER_ID)
        val userLogin = stringForKey(USER_LOGIN)
        val token = stringForKey(USER_TOKEN)

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
        const val USER_ID = "user_id"
        const val USER_LOGIN = "username"
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
