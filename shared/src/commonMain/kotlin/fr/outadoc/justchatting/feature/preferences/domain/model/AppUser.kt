package fr.outadoc.justchatting.feature.preferences.domain.model

public sealed class AppUser {
    public data class LoggedIn(
        val userId: String,
        val userLogin: String,
        val token: ApiToken,
    ) : AppUser()

    public data object NotLoggedIn : AppUser()
}
