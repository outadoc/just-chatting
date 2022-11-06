package fr.outadoc.justchatting.repository

import fr.outadoc.justchatting.model.AppUser

data class AppPreferences(
    val helixClientId: String = "l9klwmh97qgn0s0me276ezsft5szp2",
    val helixRedirect: String = "https://localhost",
    val animateEmotes: Boolean = true,
    val showTimestamps: Boolean = false,
    val recentMsgLimit: Int = 100,
    val messageLimit: Int = 600,
    val appUser: AppUser = AppUser.NotLoggedIn
) {
    object Defaults {
        const val MIN_CHAT_LIMIT = 10
        const val MAX_CHAT_LIMIT = 1_000
        const val MIN_RECENT_CHAT_LIMIT = 0
        const val MAX_RECENT_CHAT_LIMIT = 500
    }
}
