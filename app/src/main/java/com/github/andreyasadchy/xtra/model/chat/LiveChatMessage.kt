package com.github.andreyasadchy.xtra.model.chat

data class LiveChatMessage(
        override val id: String,
        override val userName: String,
        override val message: String,
        override var color: String?,
        override val isAction: Boolean,
        override val emotes: List<TwitchEmote>?,
        override val badges: List<Badge>?,
        override var globalBadge: TwitchBadge?,
        override var channelBadge: TwitchBadge?,
        val userId: Int,
        val userType: String?,
        override val displayName: String,
        val roomId: String,
        val timestamp: Long) : ChatMessage

