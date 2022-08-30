package fr.outadoc.justchatting.model

sealed class User {

    abstract val id: String?
    abstract val login: String?
    abstract val helixToken: String?

    data class LoggedIn(
        override val id: String,
        override val login: String,
        override val helixToken: String
    ) : User()

    data class NotValidated(
        override val id: String?,
        override val login: String?,
        override val helixToken: String?
    ) : User()

    object NotLoggedIn : User() {
        override val id: String? = null
        override val login: String? = null
        override val helixToken: String? = null
    }
}

fun User.NotValidated.asLoggedIn(): User.LoggedIn? {
    if (id == null || login == null || helixToken == null) return null
    return User.LoggedIn(id, login, helixToken)
}
