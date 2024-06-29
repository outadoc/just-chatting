package fr.outadoc.justchatting.feature.preferences.domain.model

internal sealed class AppUser {

    data class LoggedIn(
        val userId: String,
        val userLogin: String,
        val token: String,
    ) : AppUser()

    data class NotValidated(
        val token: String,
    ) : AppUser()

    data object NotLoggedIn : AppUser()
}
