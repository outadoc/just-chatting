package fr.outadoc.justchatting.component.twitch.model

sealed class AppUser {

    abstract val id: String?
    abstract val login: String?
    abstract val helixToken: String?

    data class LoggedIn(
        override val id: String,
        override val login: String,
        override val helixToken: String
    ) : AppUser()

    data class NotValidated(
        override val helixToken: String
    ) : AppUser() {
        override val id: String? = null
        override val login: String? = null
    }

    object NotLoggedIn : AppUser() {
        override val id: String? = null
        override val login: String? = null
        override val helixToken: String? = null
    }
}
