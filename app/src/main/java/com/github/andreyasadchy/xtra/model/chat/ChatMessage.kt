package com.github.andreyasadchy.xtra.model.chat

interface ChatMessage {
    val id: String?
    val userId: String?
    val userLogin: String?
    val userName: String?
    val message: String?
    val color: String?
    val isAction: Boolean
    val emotes: List<TwitchEmote>?
    val badges: List<Badge>?
    val fullMsg: String?
}
