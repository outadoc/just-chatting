package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.model.chat.RoomState

interface OnRoomStateReceivedListener {
    fun onRoomState(roomState: RoomState)
}
