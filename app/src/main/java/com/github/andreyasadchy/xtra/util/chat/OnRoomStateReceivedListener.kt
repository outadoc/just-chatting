package com.github.andreyasadchy.xtra.util.chat

interface OnRoomStateReceivedListener {
    fun onRoomState(list: RoomState)
}

data class RoomState(
    val emote: String?,
    val followers: String?,
    val unique: String?,
    val slow: String?,
    val subs: String?
)
