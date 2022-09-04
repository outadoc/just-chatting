package fr.outadoc.justchatting.util.chat

import fr.outadoc.justchatting.model.chat.PubSubPointReward

interface PubSubRewardParser {
    fun parse(text: String): PubSubPointReward
}
