package fr.outadoc.justchatting.component.preferences.data

sealed class AppUser {

    data class LoggedIn(
        val userId: String,
        val userLogin: String,
        val token: String,
    ) : AppUser()

    data class NotValidated(
        val token: String,
    ) : AppUser()

    object NotLoggedIn : AppUser()
}
