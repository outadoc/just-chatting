package fr.outadoc.justchatting.component.chatapi.common.pubsub

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent

interface PubSubPlugin<T : Any> {

    fun getTopic(channelId: String): String
    fun parseMessage(payload: String): List<ChatEvent>
}
