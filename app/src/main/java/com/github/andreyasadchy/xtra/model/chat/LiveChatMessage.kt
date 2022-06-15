package com.github.andreyasadchy.xtra.model.chat

import kotlinx.datetime.Instant

data class LiveChatMessage(
    override val id: String? = null,
    override val userId: String? = null,
    override val userLogin: String? = null,
    override val userName: String? = null,
    override val message: String? = null,
    override val color: String? = null,
    override val isAction: Boolean = false,
    override val emotes: List<TwitchEmote>? = null,
    override val badges: List<Badge>? = null,
    override val fullMsg: String? = null,
    val isFirst: Boolean = false,
    val msgId: String? = null,
    val systemMsg: String? = null,
    val timestamp: Instant? = null,
    val rewardId: String? = null,
    var pointReward: PubSubPointReward? = null
) : ChatMessage
