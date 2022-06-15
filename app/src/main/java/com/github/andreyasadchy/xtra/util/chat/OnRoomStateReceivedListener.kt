package com.github.andreyasadchy.xtra.util.chat

import kotlin.time.Duration

interface OnRoomStateReceivedListener {
    fun onRoomState(list: RoomState)
}

data class RoomState(
    val emote: Boolean = false,
    val followers: Duration? = null,
    val unique: Boolean = false,
    val slow: Duration? = null,
    val subs: Boolean = false
)
