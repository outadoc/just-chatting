package com.github.andreyasadchy.xtra.util.chat

import com.github.andreyasadchy.xtra.model.chat.RoomState

interface OnRoomStateReceivedListener {
    fun onRoomState(list: RoomState)
}
