package fr.outadoc.justchatting.feature.preferences.domain.model

public sealed class AppUser {
    public data class LoggedIn(
        val userId: String,
        val userLogin: String,
        val token: ApiToken,
    ) : AppUser()

    public data object NotLoggedIn : AppUser()

    /**
     * A token is saved, but we couldn't check whether it's still valid
     * (e.g. Twitch is unreachable because of a network outage).
     */
    public data object ValidationFailed : AppUser()
}
