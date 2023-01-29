package fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.plugin

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand

interface PubSubPlugin<T : Any> {

    fun getTopic(channelId: String): String
    fun parseMessage(message: String): ChatCommand?
}
