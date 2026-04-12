package fr.outadoc.justchatting.feature.chat.domain.pubsub

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent

internal interface PubSubPlugin<T : Any> {
    public fun getTopic(channelId: String): String

    public fun parseMessage(payload: String): List<ChatEvent>
}
