package com.github.andreyasadchy.xtra.util.chat

import com.github.andreyasadchy.xtra.model.chat.UserState

interface OnUserStateReceivedListener {
    fun onUserState(userState: UserState)
}
