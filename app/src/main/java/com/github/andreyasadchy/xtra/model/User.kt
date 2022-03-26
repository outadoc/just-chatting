package com.github.andreyasadchy.xtra.model

import android.content.Context
import androidx.core.content.edit
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs

sealed class User(val id: String?,
                  val login: String?,
                  val helixToken: String?,
                  val gqlToken: String?) {

    companion object {
        private var user: User? = null

        fun get(context: Context): User {
            return user ?: with(context.prefs()) {
                val id = getString(C.USER_ID, null)
                if (id != null) {
                    val name = getString(C.USERNAME, null)
                    val helixToken = getString(C.TOKEN, null)
                    val gqlToken = getString(C.GQL_TOKEN, null)
                    if (TwitchApiHelper.checkedValidation) {
                        LoggedIn(id, name, helixToken, gqlToken)
                    } else {
                        NotValidated(id, name, helixToken, gqlToken)
                    }
                } else {
                    NotLoggedIn()
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
                    putString(C.GQL_TOKEN, user.gqlToken)
                } else {
                    putString(C.USER_ID, null)
                    putString(C.USERNAME, null)
                    putString(C.TOKEN, null)
                    putString(C.GQL_TOKEN, null)
                }
            }
        }

        fun validated() {
            user = LoggedIn(user as NotValidated)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (login != other.login) return false
        if (helixToken != other.helixToken) return false
        if (gqlToken != other.gqlToken) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + login.hashCode()
        result = 31 * result + helixToken.hashCode()
        result = 31 * result + gqlToken.hashCode()
        return result
    }
}

class LoggedIn(id: String?, login: String?, helixToken: String?, gqlToken: String?) : User(id, login, helixToken, gqlToken) {
    constructor(user: NotValidated) : this(user.id, user.login, user.helixToken, user.gqlToken)
}
class NotValidated(id: String?, login: String?, helixToken: String?, gqlToken: String?) : User(id, login, helixToken, gqlToken)
class NotLoggedIn : User(null, null, null, null)