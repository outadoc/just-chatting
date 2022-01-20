package com.github.andreyasadchy.xtra.model.chat

data class LiveChatMessage(
    override val id: String? = null,
    override val userId: String? = null,
    override val userName: String? = null,
    override val displayName: String? = null,
    override val message: String? = null,
    override var color: String? = null,
    override val isAction: Boolean = false,
    override val isReward: Boolean = false,
    override val isFirst: Boolean = false,
    override val emotes: List<TwitchEmote>? = null,
    override val badges: List<Badge>? = null,
    val userType: String? = null,
    val roomId: String? = null,
    val timestamp: Long? = null) : ChatMessage

