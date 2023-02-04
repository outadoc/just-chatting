package fr.outadoc.justchatting.component.preferences.data

data class AppPreferences(
    val appUser: AppUser = AppUser.NotLoggedIn,
    val showTimestamps: Boolean = true,
    val enableRecentMessages: Boolean = true,
    val enableFfzEmotes: Boolean = true,
    val enableStvEmotes: Boolean = true,
    val enableBttvEmotes: Boolean = true,
) {
    object Defaults {
        val ChatBufferLimit = 1_000
        val RecentChatLimit = 500
    }
}
