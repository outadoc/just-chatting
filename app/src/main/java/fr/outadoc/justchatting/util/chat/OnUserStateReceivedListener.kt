package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.model.chat.UserState

interface OnUserStateReceivedListener {
    fun onUserState(userState: UserState)
}
