package com.github.andreyasadchy.xtra.model

import android.content.Context
import androidx.core.content.edit
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs

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

    companion object {
        private var user: User? = null

        fun get(context: Context): User {
            return user ?: with(context.prefs()) {
                val id = getString(C.USER_ID, null)
                if (id != null) {
                    val name = getString(C.USERNAME, null)
                    val helixToken = getString(C.TOKEN, null)
                    if (TwitchApiHelper.checkedValidation) {
                        LoggedIn(id, name!!, helixToken!!)
                    } else {
                        NotValidated(id, name, helixToken)
                    }
                } else {
                    NotLoggedIn
                }
            }.also { user = it }
        }

        fun set(context: Context, user: User?) {
            this.user = user
            context.prefs().edit {
                if (user != null) {
                    putString(C.USER_ID, user.id)
                    putString(C.USERNAME, user.login)
                    putString(C.TOKEN, user.helixToken)
                } else {
                    putString(C.USER_ID, null)
                    putString(C.USERNAME, null)
                    putString(C.TOKEN, null)
                }
            }
        }
    }
}

fun User.NotValidated.validate(): User.LoggedIn? {
    if (id == null || login == null || helixToken == null) return null
    return User.LoggedIn(id, login, helixToken)
}
