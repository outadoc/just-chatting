package fr.outadoc.justchatting.feature.preferences.domain.model

internal data class AppPreferences(
    val appUser: AppUser = AppUser.NotLoggedIn,
    val showTimestamps: Boolean = true,
    val enableRecentMessages: Boolean = true,
    val enableFfzEmotes: Boolean = true,
    val enableStvEmotes: Boolean = true,
    val enableBttvEmotes: Boolean = true,
    val enablePronouns: Boolean = true,
    val enableNotifications: Boolean = false,
) {
    object Defaults {
        const val ChatBufferLimit = 1_000
        const val RecentChatLimit = 500
    }
}
