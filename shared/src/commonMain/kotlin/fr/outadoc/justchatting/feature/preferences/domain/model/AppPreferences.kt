package fr.outadoc.justchatting.feature.preferences.domain.model

public data class AppPreferences(
    val apiToken: ApiToken? = null,
    val showTimestamps: Boolean = true,
    val showAnimatedEmotes: Boolean = true,
    val showAnimatedGifs: Boolean = true,
    val enableRecentMessages: Boolean = true,
    val enableFfzEmotes: Boolean = true,
    val enableStvEmotes: Boolean = true,
    val enableBttvEmotes: Boolean = true,
    val enablePronouns: Boolean = true,
    val enableNotifications: Boolean = false,
) {
    public object Defaults {
        public const val ChatBufferLimit: Int = 1_000
        public const val RecentChatLimit: Int = 50
    }
}
