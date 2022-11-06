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
)