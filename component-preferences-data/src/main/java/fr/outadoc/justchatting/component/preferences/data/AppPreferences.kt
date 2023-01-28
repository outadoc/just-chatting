package fr.outadoc.justchatting.component.preferences.data

data class AppPreferences(
    val animateEmotes: Boolean = true,
    val showTimestamps: Boolean = false,
    val recentMsgLimit: Int = 100,
    val messageLimit: Int = 600,
    val appUser: AppUser = AppUser.NotLoggedIn,
) {
    object Defaults {
        val ChatLimitRange = 10..1_000
        val RecentChatLimitRange = 0..500
    }
}
