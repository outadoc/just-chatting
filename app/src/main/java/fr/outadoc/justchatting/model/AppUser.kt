package fr.outadoc.justchatting.model

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
        override val id: String?,
        override val login: String?,
        override val helixToken: String?
    ) : AppUser()

    object NotLoggedIn : AppUser() {
        override val id: String? = null
        override val login: String? = null
        override val helixToken: String? = null
    }
}

fun AppUser.NotValidated.asLoggedIn(): AppUser.LoggedIn? {
    if (id == null || login == null || helixToken == null) return null
    return AppUser.LoggedIn(id, login, helixToken)
}
