package com.github.andreyasadchy.xtra.model.chat

data class PubSubPointReward(
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
    val rewardTitle: String? = null,
    val rewardCost: Int? = null,
    val rewardImage: RewardImage? = null,
    val timestamp: Long? = null) : ChatMessage {

    data class RewardImage(
        val url1: String? = null,
        val url2: String? = null,
        val url4: String? = null
    )
}

