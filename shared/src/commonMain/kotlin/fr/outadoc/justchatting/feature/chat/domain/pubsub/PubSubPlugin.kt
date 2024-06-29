package fr.outadoc.justchatting.feature.chat.domain.pubsub

import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem

internal interface PubSubPlugin<T : Any> {

    fun getTopic(channelId: String): String
    fun parseMessage(payload: String): List<ChatListItem>
}
